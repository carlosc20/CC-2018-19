import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TransfereCC extends Thread {


    CCServerSocket serverSocket;


    public TransfereCC(){
        //ServerSocket
        serverSocket = new CCServerSocket(7777);
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




    public static void main(String[] args){
        TransfereCC tcc = new TransfereCC();
        while (true){
            CCSocket con = tcc.accept();
        }
    }

    private CCSocket accept() {
        return serverSocket.accept();
    }
}
