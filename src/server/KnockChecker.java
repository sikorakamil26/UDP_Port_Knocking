package server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class KnockChecker {
    private InetAddress ipAdress;
    private Server server;
    private int howManySockets;
    private int counter = 0;
    public List<Boolean> check = new ArrayList<>();
    private List<UDPPort> sockets = new ArrayList<>();

    private Thread timeOut = new Thread(() -> {
        System.out.println("INFO: Validating knock sequence...");

        try {
            for (int i = 1; i <= 10; i++) {
                TimeUnit.MILLISECONDS.sleep(1000);
            }
            System.out.println("ERR: System timeout, declining access!");
            invalidSequence();
        } catch (InterruptedException e) {
        }
    });



    public KnockChecker(InetAddress ipAdress, Server server) {
        this.ipAdress = ipAdress;
        this.server = server;
        howManySockets = server.ports.size();
        for(int i = 0; i < howManySockets; i++){
            check.add(false);
        }
        timeOut.start();
    }

    public synchronized void check(UDPPort udpPort) {
        System.out.println("INFO: Checking");
        if(!check.get(counter) && udpPort.getPort() == server.ports.get(counter)){
            check.set(counter,true);
            System.out.println("INFO: Port number " + (counter + 1) + " is correct");
            sockets.add(udpPort);
        }else{
            System.out.println("INFO: Incorrect port!");
            counter = howManySockets - 1;
        }

        if(check.contains(false) && counter == howManySockets -1 ){
            invalidSequence();
            return;
        }

        if (!check.contains(false) && counter == howManySockets - 1){
            correctSequence();
            return;
        }

        counter++;
    }

    private void invalidSequence(){
        timeOut.interrupt();
        counter = 0;
        System.out.println("\n\n\nINFO: Wrong knock sequence sorry\n" + check + "\n\n");
        check.clear();
        for(int i = 0; i < howManySockets; i++){
            check.add(false);
        }
        server.knockCheckers.remove(ipAdress);
    }

    private void correctSequence(){
        timeOut.interrupt();
        System.out.println("INFO: Correct sequence");
        Integer randomGen = new Random().nextInt( howManySockets);
        int random = randomGen;
        sockets.get(random).sendFile();
        counter = 0;
        check.clear();
        for(int i = 0; i < howManySockets; i++){
            check.add(false);
        }
        server.knockCheckers.remove(ipAdress);
    }


}
