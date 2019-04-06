import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Tester {





    public static void main(String []args){
        sendtoServer();
    }

    private static void sendtoServer() {
        AgenteUDP udp = new AgenteUDP();
        CCPacket c = CCPacket.createQuickPack(1337,true,false,false);
        InetAddress i = null;
        try {
            i = InetAddress.getLocalHost();
            c.setDestination(i, 7777);
            udp.sendPacket(c);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
