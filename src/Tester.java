import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Tester {





    public static void main(String []args){
        sendtoServer();
    }

    private static void sendtoServer() {
        AgenteUDP udp = new AgenteUDP(8888);
        CCPacket c = CCPacket.createQuickPack(1337,true,false,false);
        InetAddress i = null;
        try {
            i = InetAddress.getLocalHost();
            c.setDestination(InetAddress.getLocalHost(), 7777);
            udp.sendPacket(c);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
