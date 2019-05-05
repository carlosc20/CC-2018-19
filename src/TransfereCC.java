import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TransfereCC {


    private static int nCon = 0;
    private static Map<Integer, CCSocket> connections = new HashMap<>();
    private static CCServerSocket serverSocket = null;

    public TransfereCC() {

    }


    public void connect(InetAddress address) throws IOException {
        CCSocket c = new CCSocket(address,7777);
        c.connect();
        connections.put(++nCon, c);
        System.out.println("Ligado a " + address.getHostAddress() + ", conexão número " + nCon);
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
        System.out.println(nCon + ": Ficheiro enviado em PUT: " + filename);
    }


    public void close(int con) throws ConnectionDoesntExistException {
        CCSocket c = connections.get(con);
        if(c == null) {
            throw new ConnectionDoesntExistException();
        }
        c.close();
        connections.remove(con);
        System.out.println("Conexão " + con + " fechada, com ip " + c.getAddress().getHostAddress());
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
            serverSocket = new CCServerSocket(7777,true);
        }
        System.out.println("Aguardando por ligações...");
        try {
            CCSocket c = serverSocket.accept();
            connections.put(++nCon, c);
            System.out.println("Ligado a " + c.getAddress().getHostAddress() + ", conexão " + nCon);
            handleRequests(c, nCon);
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
        System.out.println(con + ": Pedido GET enviado do ficheiro: " + name);
        byte[] received = c.receive();
        System.out.println(con + ": Ficheiro recebido: " + name);
        Files.write(new File(name).toPath(), received);
    }

    private void handleRequests(CCSocket c, int con) {
        try {
            while (true) {
                byte[] data = c.receive();
                if(data.length > 0) {
                    if(data[0] == 'P') {
                        String filename = "download.txt"; // TODO: ler nome de ficheiro
                        byte[] content = new byte[data.length - 1];
                        System.arraycopy(data, 1, content, 0, content.length);
                        System.out.println(con + ": PUT recebido com ficheiro: " + filename);
                        try {
                            Files.write(new File(filename).toPath(), data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(data[0] == 'G') {
                        String filename = new String(data).substring(1);
                        System.out.println(con + ": Pedido GET recebido do ficheiro: " + filename);
                        byte[] content = new byte[0];
                        try {
                            try {
                                Path fileLocation = Paths.get(filename);
                                content = Files.readAllBytes(fileLocation);
                            } catch(FileSystemNotFoundException e) {
                                System.out.println(con + ": Ficheiro não existe: " + filename + ", será enviado pacote vazio");
                            }
                            c.send(content);
                            System.out.println(con + ": Ficheiro enviado: " + filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ConnectionLostException e) {
            System.out.println(con + ": Conexão perdida: " + c.getAddress().getHostAddress());
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
