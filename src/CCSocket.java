import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CCSocket implements Runnable {

    private CCDataReceiver dataReceiver;

    //Numero de pacotes que manda no inicio
    private static int startingSendNumber = 5;

    private HashMap<Integer,CCPacket> packetBuffer = new HashMap<>();
    private InetAddress address;
    private int port;
    //Sequencia de pacs enviados
    private int sendSeq = 0;
    //Sequencia de pacs recebidos
    private int recieveSeq = 0;
    private int lastAckReceived = 0;
    private LinkedBlockingQueue<CCPacket> queue = new LinkedBlockingQueue<>();

    HashSet<Integer> acksNotSent = new HashSet<>();
    int lastAckSent = 0;

    private void calculateAck(CCPacket p){
        if(!acksNotSent.contains(p.getSequence()))
            acksNotSent.add(p.getSequence());

        while (acksNotSent.contains(lastAckSent+1))
            lastAckSent++;

        //Enviar Confirmação Ack
        CCPacket ack = CCPacket.createQuickPack(lastAckSent,p.isSYN(),true, p.isFIN());
        ack.setDestination(p.getAddress(),p.getPort());
        try {
            dataReceiver.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void run() {
        // handshake
        while(true) {
            try {
                CCPacket p = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void putPack(CCPacket p) {

        if (p.isFIN() && p.isACK()){
            CCPacket ack = CCPacket.createQuickPack(p.getSequence(),p.isSYN(),true,p.isFIN());
            ack.setDestination(p.getAddress(),p.getPort());
            try {
                dataReceiver.send(ack);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Connection End");
            dataReceiver.endConnect(address);
        }
        if (p.isACK()){
            if (p.getSequence() > lastAckReceived)
                lastAckReceived = p.getSequence();
                System.out.println("gotAck "+p.getSequence());
            return;
        }

        //Guarda-o se for um pacote novo
        if (!packetBuffer.containsKey(p.getSequence())){
            packetBuffer.put(p.getSequence(),p);
            recieveSeq++;
        }
        //Calcula ack a mandar e manda
        calculateAck(p);
    }

    public byte[] receive() {
        return null;
    }

    public void send(byte[] data) throws IOException, ConnectionLostException {
        //create ccpacket
        List<CCPacket> pacs = new ArrayList<>();
        int MTU = CCPacket.maxsize;
        int s = 0;

        while (s*MTU <= data.length){
            sendSeq++;
            CCPacket p = CCPacket.createQuickPack(sendSeq,false,false,false);
            p.setDestination(address,port);
            byte[] r = Arrays.copyOfRange(data,s*MTU,(s+1)*MTU);
            p.putData(r);
            pacs.add(p);
            s++;
        }
        send(pacs);
    }

    //So manda 1
    private void send(CCPacket p) throws IOException, ConnectionLostException {
        boolean response = false;
        int its = 0;
        while (its < 3) { //remanda
            dataReceiver.send(p);
            try {
                System.out.println("Gonna wait a sec");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Last Ack Recieved:" + lastAckReceived);
            if(lastAckReceived >= p.getSequence())
                return;
            else
                its++;
        }
        throw new ConnectionLostException();
    }

    private void send(List<CCPacket> p) throws IOException, ConnectionLostException {
        //TODO mandar mais do que um pacote
        System.out.println("Sending List:");
        int numToSend = startingSendNumber;
        int fails = 0;
        int firstSeq = p.get(0).getSequence();
        for (int i = 0; i < p.size(); ) {
            int j;
            for (j = 0; j < numToSend && i+j < p.size() ; j++) {
                dataReceiver.send(p.get(i+j));
            }
            // Wait for last pack
            try {
                System.out.println("Gonna wait for ack " +(p.get(i+j-1).getSequence()));
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Se não recebeu nenhum ack falha
            if (lastAckReceived-firstSeq+1 == i ){
                fails++;
                if (fails == 3)
                    throw new ConnectionLostException();
            }
            // Se recebeu todos manda mais packs duma vez
            if (lastAckReceived == (p.get(i+j-1).getSequence())){
                numToSend *= 2;
            }
            //se nao diminui o num de pacotes a mandar
            else if (numToSend > 1)
                numToSend -= numToSend/2;
            i = lastAckReceived - firstSeq + 1;
        }
    }


    public InetAddress getAddress() {
        return this.address;
    }

    public void connect() throws IOException {
        CCPacket synpack = CCPacket.createQuickPack(0,true,false,false);
        synpack.setDestination(address,port);
        boolean recieved = false;
        int i = 0;
        while (!recieved){
            try {
                dataReceiver.send(synpack);
                receiveHandshake();
                recieved = true;
            } catch (PacketNotRecievedException e) {
            }
            if(i>=3)
                throw new IOException("Failed to Connect");
            i++;
        }
        System.out.println("Connected!!!");
    }

    public void close(){
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
        while (!packetBuffer.containsKey(1)){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i>=3)
                throw new PacketNotRecievedException();
            i++;
        }
    }

    public void startHandshake() throws ConnectionLostException{
        CCPacket synpack = CCPacket.createQuickPack(1,true,false,false);
        synpack.setDestination(address,port);
        try {
            this.send(synpack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
