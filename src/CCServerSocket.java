import java.io.IOException;

public class CCServerSocket {

    private CCDataReceiver dataReceiver;

    public CCServerSocket(int port) {
        dataReceiver = new CCDataReceiver(port);
    }

    public CCServerSocket(int i, boolean b) {
        dataReceiver = new CCDataReceiver(i,b);
    }

    public void close(){
        dataReceiver.close();
    }
    public CCSocket accept() throws IOException {
        return dataReceiver.accept();
    }


}
