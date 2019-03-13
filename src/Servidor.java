import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class Servidor extends Thread {

    private DatagramSocket socket;
    private HashMap<SocketAddress, Transferencia> tabela;
    //Uma tabela de estado, que para cada transferência tem alguma informação útil sobre a
    // transferência e o estado da mesma, como por exemplo, ficheiro de dados,
    // origem, destino, porta de origem, porta de destino, etc.

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

            try {
                CCPacket p = receivePacket();
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
                        sendPacket(p);
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

    public CCPacket receivePacket() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new CCPacket(packet);
    }

    private void sendPacket(CCPacket p) throws IOException {
        socket.send(p.toDatagramPacket());
    }

    public static void main(String[] args) {
        Servidor t = new Servidor();
        t.run();
    }
}
