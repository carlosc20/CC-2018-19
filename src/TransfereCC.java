import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransfereCC extends Thread{

    private static AgenteUDP udp = new AgenteUDP();

    HashMap <InetAddress,CCConnection> connections = new HashMap<>();
    boolean acceptingConnections = false;
    CCPacket pendingConnection;
    ReentrantLock rl = new ReentrantLock();
    Condition waitingConection = rl.newCondition();
    LinkedBlockingQueue<CCConnection> pending = new LinkedBlockingQueue<>();
    boolean isAcceptingConnections = false;

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
        udp = new AgenteUDP();
        this.start();
    }

    public void run(){
        while (true)
            collect();
    }

    public void get(InetAddress i , String x){
        //Cria Conexão

        //Faz Pedido

        //Manda Cenas

    }

    public void put(InetAddress i , String x, String fich){

    }

    void attendConections(){
        CCConnection p = accept();
    }

    //TODO TIRAR ACCEPT
    public CCConnection accept(){
        acceptingConnections = true;
        return null;
    }

    public static void main(String args[]){
        TransfereCC tcc = new TransfereCC();
        CCConnection con = tcc.accept();
    }
}
