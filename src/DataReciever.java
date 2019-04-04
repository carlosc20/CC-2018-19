import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataReciever extends Thread{
    DatagramSocket socket;

    private static final int MTU = 256;
    private HashMap<Integer,CCPacket> pacotes = new HashMap<>();

    private void collect(){
        byte[] buf = new byte[MTU];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            byte[] data = Arrays.copyOfRange(packet.getData(),0,packet.getLength());
            CCPacket p = new CCPacket(packet);
            if (pacotes.size()==0){
                ServerHandler s =new ServerHandler(packet,this);
                s.start();
            }
            pacotes.put(p.getSequence(),p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            socket = new DatagramSocket(4337);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (true){
            collect();
        }
    }

    public static void main(String[] args){
        DataReciever d = new DataReciever();
        d.run();
        try {
            d.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(DatagramPacket pack) throws IOException {
        socket.send(pack);
    }

    public CCPacket getSeqPack(InetAddress ip, int seq) throws NoPacketRecievedException {
        throw new NoPacketRecievedException();
        //return null;
    }

    public void end(InetAddress ip) {
        pacotes = new HashMap<>();
    }
}
