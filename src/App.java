import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {

    public static void main(String []args) {
        TransfereCC tcc = new TransfereCC();
        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
        Scanner s = new Scanner(System.in);
        while(true) {
            String input = s.nextLine();
            if(input.equals("")) break;

            String[] cmds = input.split(" ");
            switch (cmds[0].toLowerCase()) {
                case "connect": {
                    if (cmds.length < 2) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    try {
                        InetAddress address = InetAddress.getByName(cmds[1]);
                        int con = tcc.connect(address);
                        System.out.println("Ligado a " + address.getHostAddress() + ", conexão número " + con);
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
                    String filename = cmds[2];
                    try {
                        tcc.put(con, filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ConnectionLostException e) {
                        System.out.println("Conexão perdida: " + e.getMessage());
                    } catch (ConnectionDoesntExistException e) {
                        System.out.println("Conexão não existe");
                    }
                }
                break;
                case "close": {
                    if (cmds.length < 2) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    int con = Integer.parseInt(cmds[1]);
                    try {
                        tcc.close(con);
                        System.out.println("Conexão " + con + " fechada");
                    } catch (ConnectionDoesntExistException e) {
                        System.out.println("Conexão não existe");
                    }
                }
                break;
                case "accept": {
                    new Thread(() -> { tcc.accept(); }).start();
                }
                break;
                case "get": {
                    if (cmds.length < 3) {
                        System.out.println("Argumentos insuficientes");
                        break;
                    }
                    int con = Integer.parseInt(cmds[1]);
                    String filename = cmds[2];
                    try {
                        tcc.get(con, filename);
                    } catch (ConnectionDoesntExistException e) {
                        System.out.println("Conexão não existe");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ConnectionLostException e) {
                        System.out.println("Conexão perdida: " + e.getMessage());
                    }
                }
                break;
                case "list": {
                    tcc.list();
                }
                break;
                default:
                    System.out.println("Comando não existe");
            }
        }
    }


}

