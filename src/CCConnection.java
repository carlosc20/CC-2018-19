import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CCConnection implements Runnable {

    private ArrayBlockingQueue<CCPacket> queue;
    private AgenteUDP agente; //sender
    private ConcurrentHashMap<InetAddress, ArrayBlockingQueue<CCPacket>> connections;
    private HashMap<Integer,CCPacket> packetBuffer;
    private InetAddress address;

    int lastrecieved = 0;


    public CCConnection(ArrayBlockingQueue<CCPacket> queue, AgenteUDP agente, ConcurrentHashMap<InetAddress, ArrayBlockingQueue<CCPacket>> connections, InetAddress address) {
        this.queue = queue;
        this.agente = agente;
        this.connections = connections;
        this.address = address;
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

        // fechar
        //connections.remove(address);
    }

    // coisas antigas:

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
