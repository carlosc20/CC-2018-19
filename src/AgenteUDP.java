import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class AgenteUDP {

    private DatagramSocket socket;
    public static final int MTU = CCPacket.headersize+CCPacket.maxsize;


    public AgenteUDP (int port){
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public AgenteUDP() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public CCPacket receivePacket() throws IOException {
        byte[] buf = new byte[MTU];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        CCPacket c = null;
        boolean recieved = false;
        while (!recieved){
            try {
                socket.receive(packet);
                c = new CCPacket(packet);
                recieved =true;
            } catch (InvalidPacketException e) {
                System.out.println("ERROR");
            }
        }
        return c;
    }

    public void sendPacket(CCPacket p) throws IOException {
        socket.send(p.toDatagramPacket());
    }
    public boolean isClosed(){
        return socket.isClosed();
    }
    public void close() {
        socket.close();
    }
}
