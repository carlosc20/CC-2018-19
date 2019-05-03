import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {

    private static int nCon = 0;
    private static Map<Integer, CCSocket> connections = new HashMap<>();

    private static CCServerSocket serverSocket = null;



    public static void main(String []args) {
        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
        Scanner s = new Scanner(System.in);
        while(true) {
            String input = s.nextLine();
            if(input == null) break;

            String[] cmds = input.split(" ");
            switch (cmds[0].toLowerCase()) {
                case "connect": {
                    if (cmds.length < 2) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    try {
                        InetAddress address = InetAddress.getByName(cmds[1]);
                        CCSocket c = new CCSocket(address,7777);
                        c.connect();
                        connections.put(++nCon, c);
                        System.out.println("Ligado a " + address.getHostAddress() + ", conexão número " + nCon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case "put": {
                    if (cmds.length < 3) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    int con = Integer.parseInt(cmds[1]);
                    CCSocket c = connections.get(con);
                    if(c == null) {
                        System.out.println("Conexão não existe");
                        break;
                    }
                    String filename = cmds[2];
                    Path fileLocation = Paths.get(filename);
                    try {
                        byte[] data = Files.readAllBytes(fileLocation);
                        c.send(data); // NAO FUNCIONA
                        //c.close();
                        System.out.println("Enviou de ficheiro: " + new String(data));
                    } catch (ConnectionLostException | IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case "close": {
                    if (cmds.length < 2) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    int con = Integer.parseInt(cmds[1]);
                    CCSocket c = connections.get(con);
                    if(c == null) {
                        System.out.println("Conexão não existe");
                        break;
                    }
                    c.close();
                    connections.remove(con);
                    System.out.println("Conexão " + con + " fechada");
                }
                case "accept": {
                    if(serverSocket == null) {
                        serverSocket = new CCServerSocket(7777);
                    }
                    CCSocket c = null;
                    try {
                        c = serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connections.put(++nCon, c);
                    System.out.println("Ligado a " + c.getAddress().getHostAddress() + ", conexão número " + nCon);
                }
            }
        }
    }


}

