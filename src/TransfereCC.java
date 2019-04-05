import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransfereCC extends Thread{

    AgenteUDP udp;

    HashMap<InetAddress,CCConnection> conections;
    boolean acceptingConnections = false;
    CCPacket pendingConnection;
    ReentrantLock rl = new ReentrantLock();
    Condition waitingConection = rl.newCondition();

    private void collect(){
        try {
            CCPacket p = udp.receivePacket();
            processPacket(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPacket(CCPacket p) {
        //Descarta se for pacote lixo. Checksum

        //Verifica se é pedido de conexao
        //
        if(p.isSYN() && acceptingConnections){
            if(!conections.containsKey(p.getAddress())){
                rl.lock();
                pendingConnection = pendingConnection;
                acceptingConnections = false;
                waitingConection.signal();
                rl.unlock();
            }
            return;
        }
        //Coloca-o em buffer se for bom e tiver conexao.
        if (conections.containsKey(p.getAddress())){
            conections.get(p.getAddress()).putPack(p);
        }

    }

    public void get(InetAddress i , String x){

    }

    public void put(InetAddress i , String x){

    }

    public CCConnection accept(){
        try {
            rl.lock();
            acceptingConnections = true;
            CCConnection c;
            while (acceptingConnections == true){
                try {
                    waitingConection.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (acceptingConnections == false){
                    //se num conexões maior que 1 meter numero de waiters
                    c = new CCConnection(pendingConnection);
                    try {
                        c.testConnected();
                    } catch (ConnectionLostException e) {
                        acceptingConnections = true;
                        e.printStackTrace();
                    }
                }
            }
            conections.put(c.getAdress(),c);
            return c;
        }
        finally {
            rl.unlock();
        }
    }

}
