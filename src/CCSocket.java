import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CCSocket {

    private CCDataReceiver dataReceiver;

    //Numero de pacotes que manda no inicio
    private static int startingSendNumber = 5;

    private ConcurrentHashMap<Integer,CCPacket> packetBuffer = new ConcurrentHashMap<>();
    private InetAddress address;
    private int port;
    //Sequencia de pacs enviados
    private volatile int sendSeq = 0;
    //Sequencia de pacs recebidos
    private volatile int recieveSeq = 0;
    private volatile int lastAckReceived = -1;

    private LinkedBlockingQueue<CCPacket> queue = new LinkedBlockingQueue<>();

    HashSet<Integer> acksNotSent = new HashSet<>();
    int lastAckSent = -1;

    private boolean recievingHandshake = false;
    //Controlo de Congestão AIMD(Aditive Increase/Multiplicative Decrese
    private int numToSend = 4;

    private synchronized void calculateAck(CCPacket p){
        if(!acksNotSent.contains(p.getSequence()))
            acksNotSent.add(p.getSequence());

        while (acksNotSent.contains(lastAckSent+1))
            lastAckSent++;

        //Enviar Confirmação Ack
        boolean sin = false, fin = false;
        if (p.getSequence() == 0)
            sin = true;
        if (p.getSequence() == lastAckSent){
            fin = p.isFIN();
        }
        CCPacket ack = CCPacket.createQuickPack(lastAckSent,sin,true, fin);
        ack.setDestination(p.getAddress(),p.getPort());
        try {
            dataReceiver.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lastAckSent>recieveSeq)
            notify();
    }

    public CCSocket (InetAddress address, int port, CCDataReceiver dRec){
        this.address = address;
        this.port = port;
        dataReceiver = dRec;
    }

    public CCSocket(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        dataReceiver = new CCDataReceiver();
        dataReceiver.putConnect(address,this);
    }

    private float alpha = 0.125f;
    private float beta = 0.25f;
    private long estimatedRTT = 0;
    private long devRTT = 0;
    //se lastAckRecieved < Key só tem tempo de saída
    //senao tem tempo total
    private HashMap<Integer,Long> sampleRTTs= new HashMap<>();
    private boolean connected = true;


    //Só adiciona o primeiro SampleRTT
    public void addToSampleRTT(int seq){
        if (sampleRTTs.containsKey(seq))
            return;
        sampleRTTs.put(seq,System.currentTimeMillis());
    }

    private void calcSampleRTT(int psequence){
        long tms = System.currentTimeMillis()-sampleRTTs.get(psequence);
        sampleRTTs.put(psequence,tms);
    }

    private synchronized void disconnect() throws ConnectionLostException {
        connected = false;
        notify();
        throw new ConnectionLostException();
    }

    private synchronized void updateTime(int prevSeq){
        //Starting
        while (prevSeq < lastAckReceived){
            prevSeq++;
            if (sampleRTTs.containsKey(prevSeq)){
                long sampleRTT = sampleRTTs.get(prevSeq);
                devRTT =  (long) ((1-beta)*(float)devRTT + beta*(float)Math.abs(sampleRTT-estimatedRTT));
                if(estimatedRTT == 0)
                    estimatedRTT = sampleRTT;
                else
                    estimatedRTT = (long) ((1-alpha)*(float) estimatedRTT + alpha*(float)sampleRTT);
            }
        }
    }

    private void waitforAck(){
        boolean waiting = true;
        long timeout = (estimatedRTT + 4*devRTT);
        if (timeout <= 0)
            timeout = 1000;
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
        if (p.isFIN() && p.isACK()){
            if (sentFinRequest){
                CCPacket ack = CCPacket.createQuickPack(p.getSequence(),p.isSYN(),true,p.isFIN());
                ack.setDestination(p.getAddress(),p.getPort());
                try {
                    dataReceiver.send(ack);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (psequence > lastAckReceived){
                    lastAckReceived = psequence;
                }
            }
            dataReceiver.endConnect(address);
            connected = false;
            System.out.println("Connection End");
            return;
        }
        if (!packetBuffer.containsKey(psequence)){
            packetBuffer.put(psequence,p);
        }

        if (p.isACK()){
            if (psequence > lastAckReceived){
                calcSampleRTT(psequence);
                lastAckReceived = psequence;
            }
            //Controlo de Congestão
            else
                numToSend -= numToSend/2;
            if (numToSend<1)
                numToSend = 4;
            return;
        }
        //Guarda-o se for um pacote novo
        //Calcula ack a mandar e manda
        calculateAck(p);
    }

    private synchronized CCPacket retrievePack() throws ConnectionLostException {
        while (lastAckSent < recieveSeq && connected) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (lastAckSent<recieveSeq)
            throw new ConnectionLostException();
        CCPacket res = packetBuffer.get(recieveSeq);
        recieveSeq++;
        return res;
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

    public synchronized void send(byte[] data) throws IOException, ConnectionLostException {
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
        send(pacs);
    }

    //So manda 1
    private void send(CCPacket p) throws IOException, ConnectionLostException {
        int fails = 0;
        while (true) { //remanda
            if (!p.isFIN())
                addToSampleRTT(p.getSequence());
            System.out.println("Sending Pack: " + sendSeq);
            dataReceiver.send(p);
            waitforAck();
            System.out.println("Last Ack Recieved: " + lastAckReceived);
            if(lastAckReceived >= p.getSequence()){
                if (!p.isFIN())
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
            System.out.println("Gonna send packs " +(p.get(i).getSequence())+" to "+p.get(i+j-1).getSequence() );
            // Wait for last pack
            waitforAck();
            System.out.println("Last Ack Recieved: " + lastAckReceived);
            int numRecieved = lastAckReceived+1-i-firstSeq;
            // Se não recebeu nenhum ack falha
            if (numRecieved == 0 ){
                fails ++;
                if(fails == 3)
                    disconnect();
            }else{
                updateTime(i+firstSeq-1);
                fails = 0;
            }
            //Aumenta o tamanho da janela
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
        } catch (IOException | ConnectionLostException e) {
            e.printStackTrace();
        }
    }

    private void receiveHandshake() throws PacketNotRecievedException{
        int i = 0;
        recievingHandshake = true;
        while (lastAckSent!=0){
            waitforAck();
            if (i>=3)
                throw new PacketNotRecievedException();
            i++;
        }
        recievingHandshake = false;
        recieveSeq++;
    }

    public void startHandshake() throws ConnectionLostException{
        CCPacket synpack = CCPacket.createQuickPack(0,true,false,false);
        synpack.setDestination(address,port);
        try {
            this.send(synpack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recieveSeq++;
    }
}
