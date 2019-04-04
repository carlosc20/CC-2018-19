import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CCPacket {

    private InetAddress address;
    private int port;
    private int sequence;
    private int size;
    private byte[] content;
    public void extractData(byte []data){
        ByteBuffer buff = ByteBuffer.wrap(data);
        size = buff.getInt(0);
        sequence = buff.getInt(4);
        content = Arrays.copyOfRange(data,8,8+size);
    }

    public CCPacket(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        extractData(packet.getData());
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
