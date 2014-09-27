/**Bootstrap server
 *
 * @author rushabhmehta
 */

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Bootstrap extends Thread {
    int count_join, count_delete, current_count;
    ArrayList<Peer> listOfPeers;
    ServerSocket serverSoc;
    int port;

    /*
     * Constructor
     */
    public Bootstrap() {
        try {
            count_delete = 0;
            count_join = 0;
            current_count = 0;
            listOfPeers = new ArrayList<>();
            port = 5000;
            serverSoc = new ServerSocket();
            serverSoc.setReuseAddress(true);
            serverSoc.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * This method creates server socket and will run till program is terminated. This method will provide booting node for new peers.
     */
    public void run() {
        try {
            while (true) {
                System.out.println("waiting for peer");
                Socket peer_join = serverSoc.accept();
                System.out.println("Peer joined");
                ObjectInputStream din = new ObjectInputStream(peer_join.getInputStream());
                String workToDo = (String) din.readObject();
                Peer p = (Peer) din.readObject();
                if (workToDo.equals("join")) {
                    p.portout = 5000 + count_join + 1;
                    if (listOfPeers.size() == 0) {
                        System.out.println("in if");
                        p.z = new Zone(new Point(0, 0), new Point(1000, 1000));
                        ObjectOutputStream out = new ObjectOutputStream(peer_join.getOutputStream());
                        out.writeObject(p);
                        out.writeObject(p.portout);
                        System.out.println("if over");
                    } else {
                        System.out.println("in else");
                        ObjectOutputStream out = new ObjectOutputStream(peer_join.getOutputStream());
                        out.writeObject(listOfPeers.get(0));
                        out.writeObject(p.portout);
                    }
                    listOfPeers.add(p);
                    count_join++;
                }
                if (workToDo.equals("delete")) {
                    int del;
                    for (int index = 0; index < listOfPeers.size(); index++) {
                        if (p.equals(listOfPeers.get(index))) {
                            del = index;
                            listOfPeers.remove(del);
                            break;
                        }

                    }
                }
                peer_join.close();

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * this is the main method
     *
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new Bootstrap().start();
    }

}
