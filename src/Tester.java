import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Tester {

    public static void main(String []args){
        try {
            CCSocket c = new CCSocket(InetAddress.getLocalHost(),7777);
            c.connect();
            byte[]data = Files.readAllBytes(Path.of("D:\\Code\\CG\\BuildEngine\\newBox.3d"));
            c.send("Stuff+Stuff+stufff".getBytes());
            c.send(data);
            c.send(data);
            c.send(data);
            c.send(data);
            c.send(data);
            c.send(data);
            c.send("Stuff+Stuff+stufff".getBytes());
            c.close();
        } catch (IOException | ConnectionLostException e) {
            e.printStackTrace();
        }
    }
}
