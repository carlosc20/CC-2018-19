import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CCSocket extends Thread{

    private CCDataReceiver dataReceiver;

    //Numero de pacotes que manda no inicio
    private static int startingSendNumber = 4;
    private static int cutNumb = 4;

    private ConcurrentHashMap<Integer,CCPacket> packetBuffer = new ConcurrentHashMap<>();
    private InetAddress address;
    private int port;
    //Sequencia de pacs enviados
    private volatile int sendSeq = 0;
    //Sequencia de pacs recebidos
    private volatile int lastAckReceived = -1;
    private int numToSend = startingSendNumber;

    private LinkedBlockingQueue<CCPacket> queue = new LinkedBlockingQueue<>();

    public CCSocket (InetAddress address, int port, CCDataReceiver dRec){
        this.address = address;
        this.port = port;
        dataReceiver = dRec;
        this.start();
    }

    public CCSocket(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        dataReceiver = new CCDataReceiver();
        dataReceiver.putConnect(address,this);
        this.start();
    }

    public void placePack(CCPacket p){
            queue.add(p);
    }

    @Override
    public void run() {
        while (connected){
            CCPacket c = null;
            try {
                c = queue.take();
                putPack(c);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    int lastAckSent = -1;



    LinkedBlockingQueue <CCPacket> retrieved = new LinkedBlockingQueue<>();




    private float alpha = 0.125f;
    private float beta = 0.25f;
    private long estimatedRTT = 0;
    private long devRTT = 0;
    private Map<Integer,Long> sentTimes= new ConcurrentHashMap<>();
    private Map<Integer,Long> sampleRTTs= new ConcurrentHashMap<>();
    private boolean connected = true;


    public void addToSampleRTT(int seq){
        if (sentTimes.containsKey(seq))
            return;
        sentTimes.put(seq,System.currentTimeMillis());
    }

    private void calcSampleRTT(int psequence){
        if (!sentTimes.containsKey(psequence))
            return;
        long tms = System.currentTimeMillis()-sentTimes.get(psequence);
        sampleRTTs.put(psequence,tms);
        sentTimes.remove(psequence);
    }

    private void disconnect() throws ConnectionLostException {
        connected = false;
        throw new ConnectionLostException();
    }

    private void updateTime(int prevSeq){
        while (prevSeq < lastAckReceived){
            prevSeq++;
            if (sampleRTTs.containsKey(prevSeq)){
                long sampleRTT = sampleRTTs.get(prevSeq);
                devRTT =  (long) ((1-beta)*(float)devRTT + beta*(float)Math.abs(sampleRTT-estimatedRTT));
                if(estimatedRTT == 0){
                    estimatedRTT = sampleRTT;
                    devRTT = sampleRTT /4;
                }
                else
                    estimatedRTT = (long) ((1-alpha)*(float) estimatedRTT + alpha*(float)sampleRTT);
                sampleRTTs.remove(prevSeq);
            }
        }
    }

    private void waitforAck() throws ConnectionLostException {
        long timeout = (estimatedRTT + 4*devRTT);
        if (timeout <= 0)
            timeout = 1000;
        if (timeout>10000)
            throw new ConnectionLostException();
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

    private boolean sentFinRequest = false;


    public void putPack(CCPacket p) {
        int psequence = p.getSequence();
        if (!p.checkChecksum())
            return;
        if (p.isFIN() && p.isACK()){
            if (sentFinRequest){
                CCPacket ack = CCPacket.createQuickPack(p.getSequence(),p.isSYN(),true,p.isFIN());
                ack.setDestination(p.getAddress(),p.getPort());
                try {
                    dataReceiver.send(ack);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dataReceiver.endConnect(address);
            connected = false;
            System.out.println("Connection End");
            return;
        }

        if (p.isACK()){
            handleAck(psequence);
            return;
        }

        if (psequence>=lastAckSent && !packetBuffer.containsKey(psequence)){
            packetBuffer.put(psequence,p);
        }
        sendAck(p);
    }

    void handleAck(int psequence){
        if (psequence > lastAckReceived){
            calcSampleRTT(psequence);
            lastAckReceived = psequence;
        }
        else{
            numToSend -= numToSend/cutNumb;
        }
        if (numToSend<startingSendNumber)
            numToSend = startingSendNumber;

        return;
    }


    CCPacket oldAck = null;

    private void sendAck(CCPacket p){
        CCPacket nextAck = oldAck;
        if (p.getSequence()>lastAckSent){
            while (packetBuffer.containsKey(lastAckSent+1)){
                lastAckSent++;
                oldAck = nextAck = packetBuffer.get(lastAckSent);
                if (!p.isSYN() && !p.isFIN() && !p.isACK())
                    retrieved.add(nextAck);
                packetBuffer.remove(lastAckSent);
            }
        }
        CCPacket ack = CCPacket.createQuickPack(nextAck.getSequence(),nextAck.isSYN(),true, nextAck.isFIN());

        ack.setDestination(p.getAddress(),p.getPort());
        try {
            dataReceiver.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CCPacket retrievePack() throws ConnectionLostException {
        if (!connected && retrieved.isEmpty()){
            throw new ConnectionLostException();
        }
        try {
            CCPacket res = retrieved.take();
            if (res.isFIN() && res.getSize() == 0)
                throw new ConnectionLostException();
            return res;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] receive() throws ConnectionLostException {
        CCPacket p = retrievePack();
        byte[] res = p.getData();
        if(p.getTotalSize() == p.getSize())
            return res;
        int sizeMissing = p.getTotalSize() - p.getSize();
        res = Arrays.copyOf(res,p.getTotalSize()+1);
        int pos = p.getData().length;
        while (sizeMissing>0) {
            p = retrievePack();
            byte[] data = p.getData();
            for (int i = 0; i < p.getSize(); i++) {
                res[pos + i] = data[i];
            }
            pos+=p.getSize();
            sizeMissing-=p.getSize();
        }
        return res;
    }

    public void send(byte[] data) throws IOException, ConnectionLostException {
        //create ccpacket
        List<CCPacket> pacs = new ArrayList<>();
        int MTU = CCPacket.maxsize;
        int s = 0;
        int sent = 0;
        while (s*MTU <= data.length){
            sendSeq++;
            CCPacket p = CCPacket.createQuickPack(sendSeq,false,false,false);
            p.setDestination(address,port);
            p.setTotalSize(data.length-sent);
            byte[] r;
            if(p.getTotalSize()>MTU)
                r = Arrays.copyOfRange(data,s*MTU,(s+1)*MTU);
            else
                r = Arrays.copyOfRange(data,s*MTU,(s*MTU+p.getTotalSize()));
            p.putData(r);
            pacs.add(p);
            sent += p.getSize();
            s++;
        }
        System.out.println("N PACKETS: " + pacs.size());
        long sd = System.currentTimeMillis();
        send(pacs);
        System.out.println((System.currentTimeMillis()-sd)/1000+" segundos");
    }

    //So manda 1
    private void send(CCPacket p) throws IOException, ConnectionLostException {
        int fails = 0;
        while (true) { //remanda
            addToSampleRTT(p.getSequence());
            System.out.println("Sending Pack: " + sendSeq);
            dataReceiver.send(p);
            waitforAck();
            System.out.println("Last Ack Recieved: " + lastAckReceived);
            if(lastAckReceived >= p.getSequence()){
                updateTime(p.getSequence()-1);
                return;
            }
            fails ++;
            if(fails == 3)
                disconnect();
        }
    }

    private void send(List<CCPacket> p) throws IOException, ConnectionLostException {
        //TODO mandar mais do que um pacote
        System.out.println("Sending List:");
        int fails = 0;
        int firstSeq = p.get(0).getSequence();
        for (int i = 0; i < p.size(); ) {
            int j;
            //Calcula tamanho da janela
            int windowSize = numToSend;
            for (j = 0; j < windowSize && i+j < p.size() ; j++) {
                CCPacket pack = p.get(i+j);
                addToSampleRTT(pack.getSequence());
                dataReceiver.send(pack);
            }

            waitforAck();
            int numRecieved = lastAckReceived+1-i-firstSeq;
            if (numRecieved == 0 ){
                fails ++;
                if(fails == 3)
                    disconnect();
            }else{
                updateTime(i+firstSeq-1);
                fails = 0;
            }
            numToSend += numRecieved;
            i = lastAckReceived - firstSeq + 1;
        }
        startingSendNumber = numToSend;
    }


    public InetAddress getAddress() {
        return this.address;
    }

    public void connect() throws IOException {
        CCPacket synpack = CCPacket.createQuickPack(0,true,false,false);
        synpack.setDestination(address,port);
        boolean recieved = false;
        int i = 0;
        long tInit = System.currentTimeMillis();
        while (!recieved){
            try {
                addToSampleRTT(0);
                dataReceiver.send(synpack);
                receiveHandshake();
                recieved = true;
            } catch (PacketNotRecievedException e) {
            } catch (ConnectionLostException e) {
                throw new IOException();
            }
            if(i>=3)
                throw new IOException("Failed to Connect");
            i++;
        }
        updateTime(-1);
        System.out.println("Connected!!!");
    }

    public void close(){
        System.out.println("CLOSING");
        sentFinRequest = true;
        CCPacket synpack = CCPacket.createQuickPack(++sendSeq,false,false,true);
        synpack.setDestination(address,port);
        try {
            this.send(synpack);
        } catch (IOException |ConnectionLostException e) {
        }
    }

    private void receiveHandshake() throws PacketNotRecievedException, ConnectionLostException {
        int i = 0;
        while (lastAckSent!=0){
            waitforAck();
            if (i>=3)
                throw new PacketNotRecievedException();
            i++;
        }
    }

    public void startHandshake() throws ConnectionLostException{
        CCPacket synpack = CCPacket.createQuickPack(0,true,false,false);
        synpack.setDestination(address,port);
        try {
            this.send(synpack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
