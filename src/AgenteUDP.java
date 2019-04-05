import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class AgenteUDP {

    private DatagramSocket socket;
    private static final int MTU = 256;

    public AgenteUDP() {
        try {
            socket = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public CCPacket receivePacket() throws IOException {
        byte[] buf = new byte[MTU];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new CCPacket(packet);
    }

    public void sendPacket(CCPacket p) throws IOException {
        socket.send(p.toDatagramPacket());
    }

    public void close() {
        socket.close();
    }
}
