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
        return serverSocket.accept();
    }

    public static void main(String[] args){
        TransfereCC tcc = new TransfereCC();
        while (true){
            CCSocket socket = tcc.accept();
            System.out.println("RETRIEVING:" +new String(socket.receive()));
        }
    }


}
