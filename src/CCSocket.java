import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CCSocket implements Runnable {

    private CCDataReceiver dataReceiver;

    private HashMap<Integer,CCPacket> packetBuffer = new HashMap<>();
    private InetAddress address;
    private int port;
    private int seq = 0;
    private int lastAckReceived = 0;
    private LinkedBlockingQueue<CCPacket> queue = new LinkedBlockingQueue<>();


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
            return;
        }

        //Guarda-o se for um pacote novo
        if (!packetBuffer.containsKey(p.getSequence())){
            packetBuffer.put(p.getSequence(),p);
            seq++;
        }
        //TODO Calcular o ack correto ... por causa de retransmições
        //Enviar Confirmação Ack
        CCPacket ack = CCPacket.createQuickPack(p.getSequence(),p.isSYN(),true, p.isFIN());
        ack.setDestination(p.getAddress(),p.getPort());
        try {
            dataReceiver.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive() {
        return null;
    }

    public void send(byte[] data){
        //create ccpacket
        //TODO
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

    private void send(List<CCPacket> p){
        //TODO mandar mais do que um pacote
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
        CCPacket synpack = CCPacket.createQuickPack(++seq,false,false,true);
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
