import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class Servidor {

    //private HashMap<SocketAddress, Transferencia> tabela;
    //Uma tabela de estado, que para cada transferência tem alguma informação útil sobre a
    // transferência e o estado da mesma, como por exemplo, ficheiro de dados,
    // origem, destino, porta de origem, porta de destino, etc.


    public Servidor() {

    }

    public static void main(String[] args) {
        AgenteUDP agenteUDP = new AgenteUDP();

        while (true) {
            try {
                CCPacket p = agenteUDP.receivePacket();
                InetAddress address = p.getAddress();
                int port = p.getPort();
                int seq = p.getSequence();
                if(seq == 0) break;

                p = new CCPacket(address, port, seq + 1);
                try {
                    agenteUDP.sendPacket(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        agenteUDP.close();
    }

}
