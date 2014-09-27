import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * zones
 *
 * @author rushabhmehta
 */
public class Zone implements Serializable {

    // String zone_name;
    Point bottom_left, top_right, center;
    HashMap<Point, ArrayList<String>> file_list = new HashMap<Point, ArrayList<String>>();

    /**
     * constructors
     */
    public Zone() {
        // TODO Auto-generated constructor stub
        bottom_left = new Point(0, 0);
        top_right = new Point(0, 0);
        center = new Point(0, 0);

    }

    /**
     * constructor
     *
     * @param p1
     * @param p2
     */
    public Zone(Point p1, Point p2) {
        // TODO Auto-generated constructor stub
        bottom_left = p1;
        top_right = p2;
        setCenter();
    }

    /**
     * display method for testing
     */
    void display() {
        System.out.println("bottom_left " + bottom_left);
        System.out.println("top_right " + top_right);
        System.out.println("center " + center);
        for (Entry<Point, ArrayList<String>> entry : file_list.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue()); // Change
            // this
            // to
            // whatever
            // you
            // need
        }
    }

    /**
     * splits the zone
     *
     * @param p
     * @return
     */
    Zone[] splitZone(Point p) {
        Zone z[] = new Zone[2];
        int zone_position = 1;
        if ((top_right.x - bottom_left.x) == (top_right.y - bottom_left.y)) {
            if (p.x > center.x) {
                zone_position = 0;
            }

            z[0 + zone_position] = new Zone(bottom_left, new Point(
                    (bottom_left.x + top_right.x) / 2, top_right.y));
            z[(1 + zone_position) % 2] = new Zone(new Point(
                    (bottom_left.x + top_right.x) / 2, bottom_left.y),
                    top_right);

            for (Entry<Point, ArrayList<String>> entry : file_list.entrySet()) {
                if (entry.getKey().x > center.x) {
                    z[(1 + zone_position) % 2].file_list.put(entry.getKey(),
                            entry.getValue());
                } else
                    z[0 + zone_position].file_list.put(entry.getKey(),
                            entry.getValue());

            }

        } else {
            if (p.y > center.y) {
                zone_position = 0;
            }

            z[0 + zone_position] = new Zone(bottom_left, new Point(top_right.x,
                    (bottom_left.y + top_right.y) / 2));
            z[(1 + zone_position) % 2] = new Zone(new Point(bottom_left.x,
                    (bottom_left.y + top_right.y) / 2), top_right);

            for (Entry<Point, ArrayList<String>> entry : file_list.entrySet()) {
                if (entry.getKey().y > center.y) {
                    z[(1 + zone_position) % 2].file_list.put(entry.getKey(),
                            entry.getValue());
                } else
                    z[0 + zone_position].file_list.put(entry.getKey(),
                            entry.getValue());
            }

        }
        return z;

    }

    /**
     * sets center of zone
     */
    public void setCenter() {
        // TODO Auto-generated method stub
        // Point p=new
        // Point((bottom_left.x+top_right.x)/2,(top_right.y+bottom_left.y)/2);
        center = new Point((bottom_left.x + top_right.x) / 2,
                (top_right.y + bottom_left.y) / 2);
    }

    /**
     * merges 2 zones
     *
     * @param incomingPeer
     * @return
     */
    public Zone merge(Peer incomingPeer) {
        // TODO Auto-generated method stub
        Point x = null, y = null;
        if (this.bottom_left.x > incomingPeer.z.bottom_left.x
                || this.bottom_left.y > incomingPeer.z.bottom_left.y)
            x = incomingPeer.z.bottom_left;
        else
            x = this.bottom_left;

        if (this.top_right.x < incomingPeer.z.top_right.x
                || this.top_right.y < incomingPeer.z.top_right.y)
            y = incomingPeer.z.top_right;
        else
            y = this.top_right;
        Zone temp = new Zone(x, y);
        temp.file_list.putAll(incomingPeer.z.file_list);
        temp.file_list.putAll(this.file_list);
        return temp;
    }
}
