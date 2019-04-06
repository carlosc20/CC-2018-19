import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransfereCC extends Thread{



    HashMap <InetAddress,CCConnection> connections = new HashMap<>();
    boolean acceptingConnections = false;
    CCPacket pendingConnection;
    ReentrantLock rl = new ReentrantLock();
    Condition waitingConection = rl.newCondition();
    LinkedBlockingQueue<CCConnection> pending = new LinkedBlockingQueue<>();
    boolean isAcceptingConnections = false;
    private AgenteUDP udp = new AgenteUDP(7777);

    //TODO COLLECT MANTEM-SE
    private void collect(){
        try {
            CCPacket p = udp.receivePacket();
            processPacket(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO PROCESSPACKET MUDA
    private void processPacket(CCPacket p) {
        // TODO: checksum

        if(p.isSYN() && !this.connections.containsKey(p.getAddress()) && isAcceptingConnections){
            CCConnection n = new CCConnection(p.getAddress(),p.getPort(),udp);
            this.connections.put(p.getAddress(),n);
            pending.add(n);
        }

        if (this.connections.containsKey(p.getAddress())){
            this.connections.get(p.getAddress()).putPack(p);
        }
    }

    public TransfereCC(){
        this.start();
    }

    public void run(){
        while (true)
            collect();
    }

    public void get(InetAddress i , String x){


    }

    public void put(InetAddress i , String x, String fich){
        //Put servers no ficheiro
    }

    //SERVERSTUFF
    void attendConections(){
        //
        CCConnection p = accept();
        //Cria server
        //start server
        //recebe pedido de ficheiro
        CCPacket pacote = p.recieve();
        //processa nome de ficheiro
        //String nomefich = new String(pacote.getData());
        //manda o ficheiro
        //p.send(ficheiro)
    }

    //TODO TIRAR ACCEPT
    public CCConnection accept(){
        acceptingConnections = true;
        boolean recieved = false;
        CCConnection c = null;
        while (!recieved){
            try {
                c = pending.take();
                c.startHandshake();
                recieved = true;
            } catch (InterruptedException | ConnectionLostException e) {

            }
        }
        acceptingConnections = false;
        return c;
    }

    public static void main(String args[]){
        TransfereCC tcc = new TransfereCC();
        CCConnection con = tcc.accept();
    }
}
