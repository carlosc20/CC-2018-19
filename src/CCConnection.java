import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

public class CCConnection {

    HashMap<Integer,CCPacket> packetBuffer;
    private InetAddress address;
    AgenteUDP u;//sender
    int lastrecieved = 0;


    public CCConnection(CCPacket pendingConnection, AgenteUDP udp) {
        u = udp;
        try {
            u.sendPacket(pendingConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putPack(CCPacket p) {
        if (p.isACK()){
            //DO STUFF ACK RELATED
            return;
        }
        if (!packetBuffer.containsKey(p.getSequence()))
            packetBuffer.put(p.getSequence(),p);
        //Enviar Confirmação Ack
    }

    public CCPacket recieve(){
        return null;
    }

    public void send (CCPacket p){

    }

    public InetAddress getAdress() {
        return this.address;
    }

    public void connect(){
        //CCPacket synPack = CCPacket.getSynPack();
        recieveHandshake();
    }

    private void recieveHandshake() {
        System.out.println("HANDSHAKE");
    }

    public void startHandshake() throws ConnectionLostException{
        System.out.println("HANDSHAKE");
        //throw new ConnectionLostException();
    }
}
