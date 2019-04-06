import java.net.InetAddress;

public class CCServerSocket {
    public static CCDataReciever dataReciever;


    //TODO TIRAR ACCEPT
    public CCSocket accept(){
        return dataReciever.accept();
    }


    public CCServerSocket(int port) {
        dataReciever = new CCDataReciever(port);
    }
}
