import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TransfereCC extends Thread {


    private static int nCon = 0;
    private static Map<Integer, CCSocket> connections = new HashMap<>();
    private static CCServerSocket serverSocket = null;

    public TransfereCC() {

    }


    public int connect(InetAddress address) throws IOException {

        CCSocket c = new CCSocket(address,7777);
        c.connect();
        connections.put(++nCon, c);
        return nCon;
    }


    public void put(int con, String filename) throws IOException, ConnectionLostException, ConnectionDoesntExistException {

        CCSocket c = connections.get(con);
        if(c == null) {
            throw new ConnectionDoesntExistException();
        }
        Path fileLocation = Paths.get(filename);
        byte[] content = Files.readAllBytes(fileLocation);
        byte[] data = new byte[content.length + 1];
        data[0] = 'P';
        System.arraycopy(content, 0, data, 1, content.length);
        c.send(data);
    }


    public void close(int con) throws ConnectionDoesntExistException {
        CCSocket c = connections.get(con);
        if(c == null) {
            throw new ConnectionDoesntExistException();
        }
        c.close();
        connections.remove(con);
    }


    public void list() {
        System.out.println("Conexões ativas:");
        List<Integer> keys = new ArrayList<>(connections.keySet());
        Collections.sort(keys);
        for (Integer key: keys) {
            System.out.println(key + ": " + connections.get(key).getAddress().getHostAddress());
        }
    }


    public void accept(){
        if (serverSocket == null) {
            serverSocket = new CCServerSocket(7777);
        }
        try {
            CCSocket c = serverSocket.accept();
            connections.put(++nCon, c);
            System.out.println("Ligado a " + c.getAddress().getHostAddress() + ", conexão número " + nCon); //??
            handleRequests(c);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void get(int con, String name) throws ConnectionDoesntExistException, IOException, ConnectionLostException {

        CCSocket c = connections.get(con);
        if(c == null) {
            throw new ConnectionDoesntExistException();
        }
        byte[] content = name.getBytes();
        byte[] data = new byte[content.length + 1];
        data[0] = 'G';
        System.arraycopy(content, 0, data, 1, content.length);
        c.send(data);

    }

    private void handleRequests(CCSocket c) {
        try {
            while (true) {
                byte[] data = c.receive();
                if(data.length > 0) {
                    if(data[0] == 'P') {
                        System.out.println("PUT data:\n" + new String(data));
                    }
                    else if(data[0] == 'G') {
                        System.out.println("GET request:\n" + new String(data));
                        String filename = new String(data);
                        Path fileLocation = Paths.get(filename);
                        byte[] content = new byte[0];
                        try {
                            if(fileLocation != null) {
                                content = Files.readAllBytes(fileLocation);
                            }
                            c.send(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ConnectionLostException e) {
            System.out.println("Conexão: " + c.getAddress().getHostAddress());
        }
    }


    private CCSocket acceptTest() {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args){
        TransfereCC tcc = new TransfereCC();
        serverSocket = new CCServerSocket(7777,true);
        while (true){
            CCSocket socket = tcc.acceptTest();
            try {
                System.out.println("RETRIEVING:\n" + new String(socket.receive()));
                System.out.println("RETRIEVING:\n" + new String(socket.receive()));
                System.out.println("\nfim");
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            }
        }
    }



}
