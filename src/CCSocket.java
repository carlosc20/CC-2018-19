import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CCSocket implements Runnable {

    CCDataReciever dataReciever;

    HashMap<Integer,CCPacket> packetBuffer = new HashMap<>();
    private InetAddress address;
    int port;
    int lastrecieved = 0;

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
        new CCDataReciever();
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
            return;
        }
        if (!packetBuffer.containsKey(p.getSequence()))
            packetBuffer.put(p.getSequence(),p);
        //Enviar Confirmação Ack
    }

    public CCPacket recieve(){
        return null;
    }

    public void send (CCPacket p){

    }

    public InetAddress getAdress() {
        return this.address;
    }

    public void connect(){
        //CCPacket synPack = CCPacket.getSynPack();
        recieveHandshake();
    }

    private void recieveHandshake() {
        System.out.println("HANDSHAKE");
    }

    public void startHandshake() throws ConnectionLostException{
        System.out.println("HANDSHAKE");
        //throw new ConnectionLostException();
    }
}