import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TransfereCC extends Thread {


    private CCServerSocket serverSocket;

    public TransfereCC() {
        serverSocket = new CCServerSocket(7777,true);
    }



    public void get(InetAddress i , String x){


    }

    public void put(InetAddress i , String x, String fich){
        //Put servers no ficheiro
    }


    //SERVERSTUFF
    void attendConections(){
        //
        //CCSocket p = accept();
        //Cria server
        //start server
        //recebe pedido de ficheiro
        //CCPacket pacote = p.recieve();
        //processa nome de ficheiro
        //String nomefich = new String(pacote.getData());
        //manda o ficheiro
        //p.send(ficheiro)
    }


    private CCSocket accept() {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args){
        TransfereCC tcc = new TransfereCC();
        while (true){
            CCSocket socket = tcc.accept();
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
