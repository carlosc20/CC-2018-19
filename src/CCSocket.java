import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CCSocket implements Runnable {

    CCDataReciever dataReciever;

    HashMap<Integer,CCPacket> packetBuffer = new HashMap<>();
    private InetAddress address;
    int port;
    int lastrecieved = 0;
    int lastAckrecieved = 0;
    private LinkedBlockingQueue<CCPacket> queue = new LinkedBlockingQueue<CCPacket>();
    private AgenteUDP agente; //sender



    public CCSocket (InetAddress address, int port, CCDataReciever dReciever){
        this.address = address;
        this.port = port;
        dataReciever = dReciever;
    }

    public CCSocket(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        dataReciever = new CCDataReciever();
        dataReciever.putConnect(address,this);
    }

    @Override
    public void run() {
        // handshake
        while(true) {
            try {
                CCPacket p = queue.take();
                System.out.println(p.getPort());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void putPack(CCPacket p) {
        if (p.isACK()){
            //DO STUFF ACK RELATED
            if (p.getSequence()> lastAckrecieved)
                lastAckrecieved = p.getSequence();
            return;
        }

        //Guarda-o se for um pacote novo
        if (!packetBuffer.containsKey(p.getSequence()))
            packetBuffer.put(p.getSequence(),p);
        //TODO Calcular o ack correto ... por causa de retransmições
        //Enviar Confirmação Ack
        CCPacket ack = CCPacket.createQuickPack(p.getSequence(),p.isSYN(),true,p.isFIN());
        ack.setDestination(p.getAddress(),p.getPort());
        try {
            dataReciever.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CCPacket recieve(){
        return null;
    }

    public void send(byte[] data){
        //create ccpacket
        //TODO
    }

    //So manda 1
    private void send (CCPacket p) throws IOException, ConnectionLostException {
        boolean response = false;
        int its = 0;
        while (!response && its < 3) { //remanda
            dataReciever.send(p);
            try {
                System.out.println("Gonna wait a sec");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Last Ack Recieved:"+lastAckrecieved);
            if(lastAckrecieved >= p.getSequence())
                response = true;
            else
                its++;
        }
        if (its >= 3)
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
                dataReciever.send(synpack);
                recieveHandshake();
                recieved = true;
            } catch (PacketNotRecievedException e) {
            }
            if(i>=3)
                throw new IOException("Failed to Connect");
            i++;
        }
        System.out.println("Connected!!!");
    }

    private void recieveHandshake() throws PacketNotRecievedException{
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
