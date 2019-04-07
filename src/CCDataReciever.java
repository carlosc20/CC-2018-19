import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CCDataReciever implements Runnable{


    private HashMap<InetAddress, CCSocket> connections = new HashMap<>();
    private LinkedBlockingQueue<CCSocket> pending = new LinkedBlockingQueue<>();
    private boolean isAcceptingConnections = false;
    private AgenteUDP udp;

    public CCDataReciever(int port) {
        udp = new AgenteUDP(port);
        Thread t = new Thread(this);
        t.start();
    }

    public CCDataReciever() {
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

        if(p.isSYN() && !this.connections.containsKey(p.getAddress()) && isAcceptingConnections){
            CCSocket n = new CCSocket(p.getAddress(),p.getPort(),this);
            this.connections.put(p.getAddress(),n);
            pending.add(n);
        }
        if (this.connections.containsKey(p.getAddress())){

            this.connections.get(p.getAddress()).putPack(p);
        }

    }

    public CCSocket accept(){
        isAcceptingConnections = true;
        boolean recieved = false;
        CCSocket c = null;
        while (!recieved){
            try {
                c = pending.take();
                try {
                    c.startHandshake();
                    System.out.println("Connected!!!");
                    recieved = true;
                } catch (ConnectionLostException e) {
                    this.connections.remove(c.getAddress());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isAcceptingConnections = false;
        return c;
    }

    public void putConnect(InetAddress address, CCSocket ccSocket) {
        this.connections.put(address,ccSocket);
    }
}
