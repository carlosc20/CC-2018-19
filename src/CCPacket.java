import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CCPacket {

    private InetAddress address;
    private int port;
    //Por esta ordem
    private byte flags; //1- syn 2- fin 4- ack 8-hellopacket
    private int totalSize = 0;
    private int sequence;
    private long checksum;
    //TODO private int checksum;
    public static int headersize = 9+8;
    public static int maxsize = 1000;
    private int size = maxsize;
    private byte data[] = null;

    public long calcChecksum(){

        int sizeb= headersize+maxsize;

        ByteBuffer b = ByteBuffer.allocate(sizeb);

        b.put(flags);
        b.putInt(totalSize);
        b.putInt(sequence);
        b.putLong(0);
        if (totalSize>0)
            b.put(data);
        //TODO meter checksum
        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(b.array(), 0, sizeb);

        // get the current checksum value
        return checksum.getValue();
    }

    public boolean checkChecksum(){
        return (checksum == calcChecksum());
    }

    public CCPacket(DatagramPacket packet) throws InvalidPacketException {
        address = packet.getAddress();
        port = packet.getPort();
        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
        flags = wrapped.get();
        totalSize = wrapped.getInt();
        if(totalSize < maxsize)
            size = totalSize;
        sequence = wrapped.getInt();
        checksum = wrapped.getLong();
        //TODO improve validação
        if (size < 0 || sequence < 0 || (flags | 7) != 7)
            throw new InvalidPacketException();
        data = new byte[size];
        wrapped.get(data);
        //check Checksum
        if(!checkChecksum()){
          if (data.length>0)
              System.out.println(sequence);
              throw new InvalidPacketException();
        }
    }


    public DatagramPacket toDatagramPacket() {
        int sizeb= headersize+size;
        ByteBuffer b = ByteBuffer.allocate(sizeb);
        b.put(flags);
        b.putInt(totalSize);
        b.putInt(sequence);
        b.putLong(calcChecksum());
        if (size>0)
            b.put(data);
        //TODO meter checksum
        byte[] buf = b.array();

        return new DatagramPacket(buf, buf.length, address, port);
    }

    public CCPacket() {
        flags = 0;
        size = 0;
        totalSize = 0;
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

    public void setDestination(InetAddress ad, int p) {
        address = ad;
        port = p;
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

    public void putData(byte[] bytes) {
        data = bytes;
        size = bytes.length;
    }

    public void printself() {
        System.out.println(flags+ "---"+ getSequence());
    }

    public byte[] getData() {
        return data;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int i) {
        totalSize= i;
    }

    public int getSize() {
        return size;
    }


}
