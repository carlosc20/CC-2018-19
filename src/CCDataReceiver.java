import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CCDataReceiver implements Runnable {


    private HashMap<InetAddress, CCSocket> connections = new HashMap<>();
    private LinkedBlockingQueue<CCSocket> pending = new LinkedBlockingQueue<>();
    private boolean isAcceptingConnections = false;
    private AgenteUDP udp;

    public CCDataReceiver(int port) {
        udp = new AgenteUDP(port);
        Thread t = new Thread(this);
        t.start();
    }

    public CCDataReceiver() {
        udp = new AgenteUDP();
        Thread t = new Thread(this);
        t.start();
    }

    public void send(CCPacket p) throws IOException {
        udp.sendPacket(p);
    }

    public void run(){
        while (true) {
            try {
                CCPacket p = udp.receivePacket();
                processPacket(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO PROCESSPACKET MUDA
    private void processPacket(CCPacket p) {
        // TODO: checksum

        if(p.isSYN() && !connections.containsKey(p.getAddress()) && isAcceptingConnections){
            CCSocket socket = new CCSocket(p.getAddress(), p.getPort(),this);
            putConnect(p.getAddress(), socket);
            pending.add(socket);
        }
        if (connections.containsKey(p.getAddress())){
            connections.get(p.getAddress()).putPack(p);
        }
    }

    public CCSocket accept(){
        isAcceptingConnections = true;
        CCSocket c;
        while (true){
            try {
                c = pending.take();
                try {
                    c.startHandshake();
                    isAcceptingConnections = false;
                    return c;
                } catch (ConnectionLostException e) {
                    this.connections.remove(c.getAddress());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void putConnect(InetAddress address, CCSocket ccSocket) {
        this.connections.put(address,ccSocket);
    }

    public synchronized void endConnect(InetAddress address) {
        this.connections.remove(address);
    }
}