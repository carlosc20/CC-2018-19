import java.net.InetAddress;
import java.util.HashMap;

public class CCConnection {

    HashMap<Integer,CCPacket> packetBuffer;
    private InetAddress address;


    public CCConnection(CCPacket pendingConnection) {

    }

    public void putPack(CCPacket p) {
        if (!packetBuffer.containsKey(p.getSequence()))
            packetBuffer.put(p.getSequence(),p);
        //Enviar Confirmação
    }


    public InetAddress getAdress() {
        return this.address;
    }

    public void testConnected() throws ConnectionLostException{
        throw new ConnectionLostException();
    }
}
