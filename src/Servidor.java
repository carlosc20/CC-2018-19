import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Servidor extends Thread {

    private DatagramSocket socket;
    // estado
    private static final int MTU = 256;
    private byte[] buf = new byte[MTU];

    public Servidor() {
        try {
            socket = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                CCPacket p = new CCPacket(packet);
                InetAddress address = p.getAddress();
                int port = p.getPort();
                int seq = p.getSequence();
                if(false) { // se está no backlog
                    if(p.isFIN()) {
                        // mandar ACK + FIN
                        // esperar por ack
                    }
                }
                else if(p.isSYN()) {
                    // começar handshake


                    System.out.println(seq);
                    // enviar SYN + ACK

                    // receber ack
                    // por cliente no backlog, se ficar cheio passar a ignorar syns
                    p = new CCPacket(address, port, seq + 1);

                    try {
                        socket.send(p.toDatagramPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        socket.close();
    }


    public static void main(String[] args) {
        Servidor t = new Servidor();
        t.run();
    }
}
