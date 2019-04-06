import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class CCConnection implements Runnable {

    HashMap<Integer,CCPacket> packetBuffer;
    private InetAddress address;
    int lastrecieved = 0;

    private ArrayBlockingQueue<CCPacket> queue;
    private AgenteUDP agente; //sender


    public CCConnection(InetAddress address, ArrayBlockingQueue<CCPacket> bq, AgenteUDP udp) {
        this.address = address;
        this.queue = bq;
        this.agente = udp;
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
