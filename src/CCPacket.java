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
        int sizeb= 1+8+size;
        ByteBuffer b = ByteBuffer.allocate(sizeb);
        b.put(flags);
        b.putInt(size);
        b.putInt(sequence);
        if (size>0)
            b.put(data);
        //TODO meter checksum
        byte[] buf = b.array();
        System.out.println("Buffer size "+buf.length);
        return new DatagramPacket(buf, buf.length, address, port);
    }

    public CCPacket() {
        flags = 0;
        size = 0;
        sequence = 0;
    }

    public static CCPacket createQuickPack(int sequence, boolean isSyn, boolean isAck, boolean isFin){
        CCPacket p = new CCPacket();
        p.setSequence(sequence);
        byte f = 0;
        if (isSyn)f |= 1;
        if (isFin)f |= 2;
        if (isAck)f |= 4;
        p.setFlags(f);
        return p;
    }

    public void setDestination(InetAddress address, int port) {
        address = address;
        port = port;
    }

    private void setSequence(int seq) {
        sequence = seq;
    }

    private void setFlags(byte i) {
        flags = i;
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

    public static void main(String[]args){
        CCPacket c = CCPacket.createQuickPack(200,true,true,false);
        if (c.isSYN())
            System.out.println("SIN");
        if (c.isFIN())
            System.out.println("FIN");
        if (c.isACK())
            System.out.println("ACK");
        System.out.println(c.getSequence());
    }
}
