import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransfereCC {

    private static AgenteUDP udp = new AgenteUDP();

    private static ConcurrentHashMap<InetAddress, ArrayBlockingQueue<CCPacket>> connections;

    private static void processPacket(CCPacket p) {
        // TODO: checksum

        InetAddress address = p.getAddress();
        ArrayBlockingQueue<CCPacket> bq = connections.get(address);

        if(bq != null) {
            bq.add(p);
        }
        else if(p.isSYN()){
            bq = new ArrayBlockingQueue<>(10);
            connections.put(address, bq);
            new CCConnection(address, bq, udp).run();
        }
    }

    public static void main(String[] args){

        while (true) {
            try {
                processPacket(udp.receivePacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
