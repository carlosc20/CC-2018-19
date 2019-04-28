import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Tester {

    public static void main(String []args){
        try {
            CCSocket c = new CCSocket(InetAddress.getLocalHost(),7777);
            c.connect();
            c.send("Stuff+Stuff".getBytes());
            c.close();
        } catch (IOException | ConnectionLostException e) {
            e.printStackTrace();
        }
    }
}
