import java.io.IOException;
import java.net.*;

public class Client {
    private static DatagramSocket socket;
    private static InetAddress address;

    private byte[] buf;

    public Client() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 7777);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // enviar syn
        // se der timeout reenviar
        // quando recebe confirmação enviar ACK

        packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(packet.getData(), 0, packet.getLength());
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
        Client c = new Client();
        System.out.println(c.sendEcho("teste"));
        System.out.println(c.sendEcho("end"));
    }
}
