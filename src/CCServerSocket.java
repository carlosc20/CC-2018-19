
public class CCServerSocket {

    private CCDataReceiver dataReceiver;

    public CCServerSocket(int port) {
        dataReceiver = new CCDataReceiver(port);
    }

    public CCSocket accept(){
        return dataReceiver.accept();
    }


}
