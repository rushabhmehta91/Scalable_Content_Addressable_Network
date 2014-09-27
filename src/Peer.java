/**
 * peer class
 * @author rushabhmehta
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Peer extends Thread implements Serializable, ActionListener {
    String peer_name;
    InetAddress ipaddr;
    Zone z;
    Zone secondary;
    int portin, portout;
    ArrayList<String> route;
    boolean flag, secondaryZone;
    ArrayList<Peer> neighbours;
    JFrame frame;
    boolean isJoin = false, isleave = false;
    JTextField textEntry;
    JFileChooser fc;

    /**
     * constructor
     */

    public Peer() {

        try {
            peer_name = null;
            ipaddr = InetAddress.getLocalHost();
            portin = 5000;
            portout = 54321;
            z = new Zone();
            neighbours = new ArrayList<>();
            route = new ArrayList<>();
            flag = true;
            secondaryZone = false;
            frame = new JFrame();
            textEntry = new JTextField();
            fc = new JFileChooser();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * compares of 2 peers
     *
     * @param p
     * @return
     */
    boolean equals(Peer p) {
        if (p.peer_name.equals(this.peer_name) && p.ipaddr.equals(this.ipaddr))
            return true;
        else
            return false;
    }

    /**
     * toString of the peer
     */
    public String toString() {
        String x = "";
        for (int index = 0; index < neighbours.size(); index++)
            x = x + neighbours.get(index).peer_name + " ";
        return "<html>Peer Details: <br/>Name: " + peer_name
                + "<br/>Ip Address: " + ipaddr + "<br>Port No: " + portout
                + "<br/>neighbours :" + x + "<br/>" + z.bottom_left + "<br/>"
                + z.top_right + "<br/>" + z.center + "<br/>files: "
                + z.file_list + "</html>";

    }

    /**
     * finds target peer to which new peer will join
     */
    public void joinPeer() {
        try {
            Socket sock = new Socket("192.168.1.105", portin);
            ObjectOutputStream out = new ObjectOutputStream(
                    sock.getOutputStream());
            out.writeObject("join");
            out.writeObject(this);
            ObjectInputStream din = new ObjectInputStream(sock.getInputStream());
            Peer boot = (Peer) din.readObject();
            portout = (int) din.readObject();

            // Joining of first peer
            if (this.equals(boot)) {
                this.z = boot.z;
                this.portout = boot.portout;
                this.display();
                this.start();
                // this.server();
            }
            // joining of second peer onwards
            else {
                neighbours.add(boot);
                Point random = new Point(
                        (int) (System.currentTimeMillis() % 1000),
                        (int) (System.currentTimeMillis() % 1000));
                // System.out.println("navigate");

                Peer temp = navigate(random, "join");
                neighbours.clear();
                joinRequest(temp, random);
                System.out.println("PEER JOINED....");
                neighbours = modifyNeighbours(temp);
                // System.out.println(neighbours);
                this.display();
                this.start();

            }
            // port=(int) din.readObject();
            sock.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * joins to target peer
     *
     * @param temp
     * @param random
     */
    private void joinRequest(Peer temp, Point random) {
        // TODO Auto-generated method stub
        Socket sock;
        ObjectOutputStream out;
        try {
            sock = new Socket(temp.ipaddr, temp.portout);
            out = new ObjectOutputStream(sock.getOutputStream());
            out.writeObject("joinRequest");
            out.writeObject(random);
            out.writeObject(this);
            ObjectInputStream din = new ObjectInputStream(sock.getInputStream());
            Peer p = (Peer) din.readObject();
            // p.display();
            this.z = p.z;
            this.neighbours = p.neighbours;
            // this.display();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * splits the peer so new peer can join
     *
     * @param joiningPeer
     * @param dst
     * @return
     */
    Peer spliting(Peer joiningPeer, Point dst) {
        System.out.println("splitting peer");
        Zone temp[] = new Zone[2];
        temp = z.splitZone(dst);
        z = temp[0];
        joiningPeer.z = temp[1];
        neighbours = modifyNeighbours(this);
        neighbours.add(joiningPeer);
        joiningPeer.neighbours.add(this);
        return joiningPeer;
    }

    /**
     * Modify neighbours after join
     *
     * @param p
     * @return
     */
    private ArrayList<Peer> modifyNeighbours(Peer p) {

        System.out.println("spliting neighbours");
        ArrayList<Peer> temp1 = new ArrayList<>(neighbours);
        // System.out.println("spliting " + p.peer_name + " " + this.peer_name);
        // System.out.println(temp1);
        // System.out.println(p.neighbours);
        for (int index = 0; index < p.neighbours.size(); index++) {
            System.out.println(p.neighbours);
            Peer temp = p.neighbours.get(index);
            // System.out.println("spliting " + temp.peer_name + " "
            // + this.peer_name);
            if (this.equals(temp))
                continue;
            Socket sock;
            try {
                sock = new Socket(temp.ipaddr, temp.portout);
                ObjectOutputStream out = new ObjectOutputStream(
                        sock.getOutputStream());
                ObjectInputStream din = new ObjectInputStream(
                        sock.getInputStream());
                out.writeObject("checkMeNeighbour");
                out.writeObject(this);
                String x = (String) din.readObject();
                if (x.equals("true")) {
                    boolean flagcheck = true;
                    for (int index1 = 0; index1 < temp1.size(); index1++) {
                        if (temp.equals(temp1.get(index1))) {
                            flagcheck = false;
                            break;
                        }
                    }
                    if (flagcheck) {
                        temp1.add(temp);
                    }

                } else {
                    // System.out.println("in else1");
                    int del;
                    for (int index1 = 0; index1 < temp1.size(); index1++) {
                        if (temp.equals(temp1.get(index1))) {
                            del = index1;
                            temp1.remove(del);
                            break;
                        }
                    }

                }
                // if (isNeighbour(temp, joiningPeer)) {
                // temp2.add(temp);
                // }

                sock.close();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return temp1;
        // return temp2;
    }

    /**
     * check whether one peer is neighbour of other
     *
     * @param temp
     * @param peer
     * @return
     */
    private boolean isNeighbour(Peer temp, Peer peer) {
        // TODO Auto-generated method stub
        // System.out.println("checking" + peer.peer_name + " " +
        // temp.peer_name);
        // left Neighbour
        if (temp.z.top_right.x == peer.z.bottom_left.x
                && temp.z.top_right.y > peer.z.bottom_left.y
                && temp.z.bottom_left.y < peer.z.top_right.y
                && temp.z.bottom_left.x < peer.z.top_right.x) {
            System.out.println("left neighbour");
            return true;
        }

        // up Neighbour
        if (temp.z.bottom_left.y == peer.z.top_right.y
                && temp.z.top_right.x > peer.z.bottom_left.x
                && temp.z.top_right.y > peer.z.bottom_left.y
                && temp.z.bottom_left.x < peer.z.top_right.x) {
            System.out.println("up neighbour");
            return true;
        }

        // right Neighbour
        if (temp.z.bottom_left.x == peer.z.top_right.x
                && temp.z.bottom_left.y < peer.z.top_right.y
                && temp.z.top_right.x > peer.z.bottom_left.x
                && temp.z.top_right.y > peer.z.bottom_left.y) {
            System.out.println("right neighbour");
            return true;
        }

        // bottom Neighbour
        if (temp.z.top_right.y == peer.z.bottom_left.y
                && temp.z.bottom_left.x < peer.z.top_right.x
                && temp.z.bottom_left.y < peer.z.top_right.y
                && temp.z.top_right.x > peer.z.bottom_left.x) {
            System.out.println("bottom neighbour");
            return true;
        }

        System.out.println("not a neighbour");
        return false;
    }

    /**
     * navigate to destination point.
     *
     * @param dst
     * @param keyword
     * @return
     */
    Peer navigate(Point dst, String keyword) {
        System.out.println(dst);

        if (z != null && dst.x > this.z.bottom_left.x
                && dst.x < this.z.top_right.x && dst.y > this.z.bottom_left.y
                && dst.y < this.z.top_right.y) {
            if (keyword.equals("join")) {
                System.out.println("i reached");
                route.add(peer_name);

            }
            return this;
        } else {
            Peer tempreturn = null;
            double min_distance = 10000;
            Peer closed_peer = null;
            for (int index = 0; index < neighbours.size(); index++) {

                Peer temp = neighbours.get(index);
                // temp.display();
                double distance = temp.z.center.distance(dst);
                // System.out.println("distance " + distance);
                if (distance < min_distance) {
                    min_distance = distance;
                    closed_peer = temp;
                }
            }
            Socket sock;
            try {
                sock = new Socket(closed_peer.ipaddr, closed_peer.portout);
                ObjectOutputStream out = new ObjectOutputStream(
                        sock.getOutputStream());
                out.writeObject(keyword);
                out.writeObject(dst);
                ObjectInputStream din = new ObjectInputStream(
                        sock.getInputStream());
                tempreturn = (Peer) din.readObject();
                route = (ArrayList<String>) din.readObject();
                route.add(peer_name);
                sock.close();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return tempreturn;
        }
    }

    /**
     * display function for testing
     */
    void display() {
        System.out.println("Name " + peer_name);
        // System.out.println("portin " + portin);
        // System.out.println("portout " + portout);
        // System.out.println("ip address " + ipaddr);
        System.out.println("Neighours ");

        for (int index = 0; index < neighbours.size(); index++)
            System.out.print(" " + neighbours.get(index).peer_name);
        System.out.println();
        if (z != null)
            z.display();
    }

    // void server() {
    //
    // try {
    // BufferedReader br = new BufferedReader(new InputStreamReader(
    // System.in));
    // while (flag) {
    //
    // int choice = Integer.parseInt(br.readLine());
    // display();
    // }
    //
    // }
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    //
    // }
    //
    // System.out.println("peer turning off");
    // }
    //

    /**
     * run function which keeps serversocket ready for accept
     */
    public void run() {

        try {
            ServerSocket PeerserverSoc;
            PeerserverSoc = new ServerSocket();
            PeerserverSoc.setReuseAddress(true);
            PeerserverSoc.bind(new InetSocketAddress(ipaddr, portout));
            while (flag) {

                System.out.println("Waiting.....");
                Socket sock = PeerserverSoc.accept();
                if (!flag)
                    break;
                else {
                    ObjectInputStream din = new ObjectInputStream(
                            sock.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(
                            sock.getOutputStream());
                    String keyword = (String) din.readObject();
                    Point dst;
                    Peer incomingPeer = null;
                    switch (keyword) {
                        case "join":
                            joinExtracted(din, keyword, out);
                            break;
                        case "joinRequest":
                            joinrequest(din, out);
                            // display();
                            break;
                        case "removeNeighbour":
                            removeNeighbourExtracted(din);
                            break;
                        case "checkMeNeighbour":
                            checkMeNeighboursExtracted(din, out);
                            break;
                        case "shareFile":
                            shareFileExtracted(din, out);
                            break;

                        case "searchFile":
                            searchFileExtracted(din, out);
                            break;

                        case "requestfortakingover":
                            requestfortakingoverExtracted(din, out);
                            break;
                        case "mergeZone":
                            sock = mergeZoneExtracted(sock, din, out);
                            break;
                    }

                    sock.close();
                    display();
                    frame.dispose();
                    addComponent();
                }
            }
            PeerserverSoc.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Peer is turning off its server");
    }

    /**
     * this method is call when peer requests for taking over
     *
     * @param din
     * @param out
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void requestfortakingoverExtracted(ObjectInputStream din,
                                               ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Peer incomingPeer;
        incomingPeer = (Peer) din.readObject();
        for (int index = 0; index < neighbours.size(); index++) {
            if (incomingPeer.equals(neighbours.get(index))) {
                int del = index;
                neighbours.remove(del);
                break;
            }
        }
        display();
        out.writeObject((this.z.top_right.x - this.z.bottom_left.x)
                * (this.z.top_right.y - this.z.bottom_left.y));
    }

    /**
     * this method is call when other peer wants to share file to this peer
     *
     * @param din
     * @param out
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void shareFileExtracted(ObjectInputStream din,
                                    ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Point dst;
        System.out.println("shareFile");
        dst = (Point) din.readObject();
        String f = (String) din.readObject();
        String x = addFile(dst, f);
        out.writeObject(x);

        // + " downloaded (" + current + " bytes read)");
    }

    /**
     * this method asks neighbours whether they are still their his neighbour
     *
     * @param din
     * @param out
     * @return
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private int checkMeNeighboursExtracted(ObjectInputStream din,
                                           ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Peer incomingPeer;
        incomingPeer = (Peer) din.readObject();
        int index;
        if (isNeighbour(incomingPeer, this)) {

            for (index = 0; index < neighbours.size(); index++) {
                if (incomingPeer.equals(neighbours.get(index))) {
                    // System.out.println("for if");
                    out.writeObject("true");
                    break;
                }
            }
            // System.out.println("for end");
            if (index == neighbours.size()) {
                // System.out.println("if");
                neighbours.add(incomingPeer);
                out.writeObject("true");
            }
        } else {
            // System.out.println("else");
            int del;
            for (index = 0; index < neighbours.size(); index++) {
                if (incomingPeer.equals(neighbours.get(index))) {
                    del = index;
                    neighbours.remove(del);
                    break;
                }
            }
            out.writeObject("false");
        }
        return index;
    }

    /**
     * this method helps in navigation of request
     *
     * @param din
     * @param keyword
     * @param out
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void joinExtracted(ObjectInputStream din, String keyword,
                               ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Point dst;
        System.out.println("join");
        dst = (Point) din.readObject();
        out.writeObject(navigate(dst, keyword));
        out.writeObject(route);
        route.clear();

    }

    /**
     * this method replies to search file requests
     *
     * @param din
     * @param out
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void searchFileExtracted(ObjectInputStream din,
                                     ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Point dst;
        String f;
        ArrayList<String> value = new ArrayList<>();
        dst = (Point) din.readObject();
        f = (String) din.readObject();
        value = z.file_list.get(dst);
        if (value != null && value.contains(f))
            out.writeObject("file found");
        else
            out.writeObject("file not found");
    }

    /**
     * this method relpies to mergezone request at the time of leave
     *
     * @param sock
     * @param din
     * @param out
     * @return
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private Socket mergeZoneExtracted(Socket sock, ObjectInputStream din,
                                      ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Peer incomingPeer;
        incomingPeer = (Peer) din.readObject();
        String zoneType = (String) din.readObject();
        if (zoneType.equals("secondary")) {
            secondaryZone = true;
            secondary = incomingPeer.z;
            for (int index = 0; index < incomingPeer.neighbours.size(); index++) {

                Peer temp = incomingPeer.neighbours.get(index);
                if (temp.equals(this))
                    continue;
                if (this.neighbours.contains(temp))
                    continue;
                this.neighbours.add(temp);
                sock = new Socket(temp.ipaddr, temp.portout);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.writeObject("addNeighbour");
                out.writeObject(this);
            }

        } else {
            this.z = z.merge(incomingPeer);
            modifyNeighbours(incomingPeer);

        }
        return sock;
    }

    /**
     * this method removes those peer from neighbour list which are no longer
     * neighbours
     *
     * @param din
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void removeNeighbourExtracted(ObjectInputStream din)
            throws IOException, ClassNotFoundException {
        Peer incomingPeer;
        incomingPeer = (Peer) din.readObject();
        int del;
        for (int index = 0; index < neighbours.size(); index++) {
            if (incomingPeer.equals(neighbours.get(index))) {
                del = index;
                neighbours.remove(del);
                break;
            }
        }

    }

    /**
     * this method processes join requests
     *
     * @param din
     * @param out
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void joinrequest(ObjectInputStream din, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Point dst;
        Peer incomingPeer;
        System.out.println("joinRequest");
        dst = (Point) din.readObject();
        incomingPeer = (Peer) din.readObject();
        out.writeObject(spliting(incomingPeer, dst));

    }

    /**
     * this method file in file list
     *
     * @param dst
     * @param f
     * @return
     */
    private String addFile(Point dst, String f) {
        // TODO Auto-generated method stub
        ArrayList<String> value = new ArrayList<>();
        String f1 = null;
        if (!z.file_list.containsKey(dst)) {
            value.add(f);
            z.file_list.put(dst, value);
            f1 = "file shared successful";
        } else {
            value = z.file_list.get(dst);
            if (!value.contains(f)) {
                value.add(f);
                z.file_list.put(dst, value);
                f1 = "file shared successful";
            } else
                f1 = "file shared unsuccessful";
        }
        return f1;
    }

    /**
     * calculates x'
     *
     * @param key
     * @return
     */
    public int charAtOdd(String key) {
        int x = 0;
        for (int index = 0; index < key.length(); index = index + 2) {
            x += key.charAt(index);
        }
        return (x % 1000);
    }

    /**
     * calculates y'
     *
     * @param key
     * @return
     */
    public int charAtEven(String key) {
        int y = 0;
        for (int index = 1; index < key.length(); index = index + 2) {
            y += key.charAt(index);
        }
        return (y % 1000);
    }

    /**
     * makes peer leave
     */
    private void leavePeer() {
        // TODO Auto-generated method stub
        Socket sock;
        ObjectOutputStream out;
        ObjectInputStream din;
        try {
            Peer takingover = null, takingover1 = null;
            int area = (this.z.top_right.x - this.z.bottom_left.x)
                    * (this.z.top_right.y - this.z.bottom_left.y);
            int minarea = 1000 * 10000;
            for (int index = 0; index < neighbours.size(); index++) {
                Peer temp = neighbours.get(index);

                sock = new Socket(temp.ipaddr, temp.portout);
                out = new ObjectOutputStream(sock.getOutputStream());
                //
                out.writeObject("requestfortakingover");
                out.writeObject(this);
                din = new ObjectInputStream(sock.getInputStream());
                int neighbourarea = (int) (din.readObject());
                System.out.println("neig");
                if (minarea > neighbourarea) {
                    minarea = neighbourarea;
                    takingover1 = temp;
                }

                if (area == neighbourarea) {
                    takingover = temp;

                }
                // sock.close();
            }

            if (takingover == null) {
                sock = new Socket(takingover1.ipaddr, takingover1.portout);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.writeObject("mergeZone");
                out.writeObject(this);
                out.writeObject("secondary");
                System.out.println("sec");
            } else {
                sock = new Socket(takingover.ipaddr, takingover.portout);
                out = new ObjectOutputStream(sock.getOutputStream());
                out.writeObject("mergeZone");
                out.writeObject(this);
                out.writeObject("normal");
                System.out.println("normal");
            }

            // sock.close();
            sock = new Socket(this.ipaddr, this.portout);
            // sock.close();
            sock = new Socket("129.21.117.76", portin);
            out = new ObjectOutputStream(sock.getOutputStream());
            out.writeObject("delete");
            out.writeObject(this);
            sock.close();
            System.out.println("ds");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        flag = false;

    }

    /**
     * search file with filename
     *
     * @param filename
     * @return
     */
    private String searchFile(String filename) {
        int x = charAtOdd(filename);
        int y = charAtEven(filename);
        Point dst = new Point(x, y);
        String f = null;
        Peer p = navigate(dst, "join");
        if (p.equals(this)) {
            ArrayList<String> value = z.file_list.get(dst);
            if (value != null && value.contains(filename))
                f = "file found";
            else
                f = "file not found";
        } else {
            // ObjectInputStream din;
            try {
                Socket sock = new Socket(p.ipaddr, p.portout);
                ObjectOutputStream out = new ObjectOutputStream(
                        sock.getOutputStream());
                out.writeObject("searchFile");
                out.writeObject(dst);
                out.writeObject(filename);
                ObjectInputStream din = new ObjectInputStream(
                        sock.getInputStream());
                f = (String) din.readObject();
                System.out.println(f);
                sock.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // TODO Auto-generated method stub
        return f;
    }

    /**
     * shares fie with filename
     *
     * @param path
     * @return
     */
    public String shareFile(String path) {
        String filename = path;
        String f = null;
        // path.substring(path.lastIndexOf("\\"));
        int x = charAtOdd(filename);
        int y = charAtEven(filename);
        Point dst = new Point(x, y);
        Peer p = navigate(dst, "join");
        if (p.equals(this)) {
            f = addFile(dst, path);
            frame.dispose();
            addComponent();
        } else {

            try {
                System.out.println("in else" + p.ipaddr + " " + p.portout);
                Socket sock = new Socket(p.ipaddr, p.portout);
                ObjectOutputStream out = new ObjectOutputStream(
                        sock.getOutputStream());
                out.writeObject("shareFile");
                out.writeObject(dst);
                out.writeObject(filename);
                ObjectInputStream din = new ObjectInputStream(
                        sock.getInputStream());
                f = (String) din.readObject();
                sock.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        System.out.println(f);
        return f;
    }

    /**
     * main function
     *
     * @param args
     */
    public static void main(String[] args) {
        Peer p = new Peer();
        p.addComponent();
        ;

    }

    /**
     * generates GUI
     */
    public void addComponent() {
        frame = new JFrame("Peer");
        JPanel panel;
        JButton button;

        JLabel l, l1;
        panel = new JPanel();
        button = new JButton();

        l = new JLabel();
        l1 = new JLabel();
        // panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        if (isleave)
            isJoin = false;
        textEntry = new JTextField();
        if (isJoin == false) {
            textEntry = new JTextField("Enter the name");
        } else {
            textEntry = new JTextField("enter file name");
        }

        constraints.fill = constraints.HORIZONTAL;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 0, 0, 10);
        // button.addActionListener(this);
        panel.add(textEntry, constraints);

        button = new JButton("Browse");
        constraints.fill = constraints.NONE;
        constraints.gridwidth = constraints.REMAINDER;
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 0, 10, 0);
        button.addActionListener(this);
        panel.add(button, constraints);
        if (isJoin == false) {
            button.setEnabled(true);
        }

        button = new JButton("Join");
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 0, 0, 0);
        button.addActionListener(this);
        panel.add(button, constraints);
        if (isJoin == true) {
            button.setEnabled(false);
        }
        button = new JButton("Share");
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 10, 0, 0);
        button.addActionListener(this);
        panel.add(button, constraints);
        if (isJoin == false) {
            button.setEnabled(false);
        }
        button = new JButton("Search");
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 10, 0, 0);
        button.addActionListener(this);
        panel.add(button, constraints);
        if (isJoin == false) {
            button.setEnabled(false);
        }
        button = new JButton("Leave");
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 10, 0, 0);
        button.addActionListener(this);
        panel.add(button, constraints);
        if (isJoin == false) {
            button.setEnabled(false);
        }
        if (isJoin == true) {
            l = new JLabel(toString());
            constraints.gridwidth = constraints.REMAINDER;
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.insets = new Insets(50, 0, 0, 0);
            button.addActionListener(this);
            panel.add(l, constraints);
        }
        if (isJoin == true) {
            l1 = new JLabel();
            constraints.gridwidth = constraints.REMAINDER;
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.insets = new Insets(50, 0, 0, 0);
            button.addActionListener(this);
            panel.add(l, constraints);
        }

        frame.getContentPane().add(panel);
        frame.setResizable(false);

        frame.pack();
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Action listener on Buttons on gui
     */
    public void actionPerformed(ActionEvent e) {
        String name = e.getActionCommand();
        JFrame f = new JFrame();
        String routefound;
        switch (name) {
            case "Join":
                isJoin = true;
                this.peer_name = textEntry.getText();
                joinPeer();
                // l1.setText("t");
                routefound = displayRoute();
                JOptionPane.showMessageDialog(frame,
                        "<html>Peer Joined!!!!!!<br/> Route: " + routefound
                                + "</html>");
                frame.dispose();
                addComponent();

                break;
            case "Browse":
                fc = new JFileChooser();

                f.add(fc);
                f.pack();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                int result = fc.showOpenDialog(f);
                if (result == JFileChooser.APPROVE_OPTION) {
                    textEntry.setText(fc.getSelectedFile().getAbsolutePath());
                    f.dispose();
                } else if (result == JFileChooser.CANCEL_OPTION) {
                    f.dispose();
                }

                break;
            case "Share":
                String path = textEntry.getText();
                // shareFile(path);
                routefound = displayRoute();
                JOptionPane.showMessageDialog(frame, "<html>" + shareFile(path)
                        + "<br/> Route: " + routefound + "</html>");
                break;
            case "Search":
                String filename = textEntry.getText();
                routefound = displayRoute();
                JOptionPane.showMessageDialog(frame, "<html>"
                        + searchFile(filename) + "<br/> Route: " + routefound
                        + "</html>");
                break;
            case "Leave":
                isJoin = false;
                JOptionPane.showMessageDialog(frame, "Peer turning of");
                frame.dispose();
                addComponent();
                break;
        }

    }

    /**
     * calculates route to display
     *
     * @return
     */
    private String displayRoute() {
        String routeFound = "";
        for (int index = route.size() - 2; index >= 0; index--)
            routeFound = routeFound + route.get(index) + " ";
        // TODO Auto-generated method stub
        route.clear();
        return routeFound;
    }
}