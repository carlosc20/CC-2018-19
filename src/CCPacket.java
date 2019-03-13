import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class CCPacket {

    private InetAddress address;
    private int port;
    private int sequence;

    public CCPacket(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
        wrapped.getInt(0);
    }

    public CCPacket(InetAddress address, int port, int sequence) {
        this.address = address;
        this.port = port;
        this.sequence = sequence;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getSequence() {
        return sequence;
    }

    public DatagramPacket toDatagramPacket() {

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(sequence);
        byte[] buf = b.array();
        return new DatagramPacket(buf, buf.length, address, port);
    }

    public boolean isSYN() {
        return true;
    }

    public boolean isFIN() {
        return false;
    }
}
