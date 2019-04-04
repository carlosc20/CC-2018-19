import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ServerHandler extends Thread{
    DatagramPacket pack;
    DataReciever reciever;
    InetAddress ip;
    int port;

    public ServerHandler(DatagramPacket packet, DataReciever dataReciever) {
        pack = packet;
        reciever = dataReciever;
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reciever.end(ip);
    }

    private void connect() throws IOException {
        //pack.setData();
        boolean response = false;
        int its = 0;
        while (!response && its < 3) { //remanda
            reciever.send(pack);
            int seq = 1;
            try {
                System.out.println("Gonna wait a sec");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                CCPacket p = reciever.getSeqPack(ip,seq);
                response = true;
            } catch (NoPacketRecievedException e) {
                its++;
            }
        }
        if (its >= 3)
            System.out.println("CONNECT LOST");
    }
}
