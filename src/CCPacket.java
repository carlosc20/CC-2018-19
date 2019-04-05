import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class CCPacket {

    private InetAddress address;
    private int port;
    //Por esta ordem
    private byte flags; //1- syn 2- fin 4- ack 8-hellopacket
    private int size;
    private int sequence;
    //TODO private int checksum;
    private byte data[];

    public CCPacket(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
        flags = wrapped.get(0);
        size = wrapped.getInt(1);
        sequence = wrapped.getInt(5);
        //TODO ler checksum, ajustar data place
        wrapped.get(data,6,size);
        wrapped.getInt(0);
    }

    public DatagramPacket toDatagramPacket() {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.put(flags);
        b.putInt(size);
        b.putInt(sequence);
        b.put(data);
        //TODO meter checksum
        byte[] buf = b.array();
        return new DatagramPacket(buf, buf.length, address, port);
    }


    public CCPacket(InetAddress address, int port, int sequence) {
        this.address = address;
        this.port = port;
        this.sequence = sequence;
    }


    public CCPacket getAckPacket(){
       // CCPacket acker = new CCPacket();
        return null;
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



    public boolean isSYN() {
        int flag = flags;
        int syn = flag & 1;
        return syn > 0;
    }

    public boolean isACK() {
        int flag = flags;
        int ack = flag & 4;
        return ack > 0;
    }

    public boolean isFIN() {
        int flag = flags;
        int fin = flag & 2;
        return fin > 0;
    }
}
