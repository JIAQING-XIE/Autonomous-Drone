package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.*;

/**
 * This class includes a constructor, two distance metrics functions, one judging function,
 * the function to realize A* algorithm with a comparator and the function to calculate the angles
 * of each move(path).
 */
public class Node {
    public Node(double lng, double lat) {
        this.lng = lng; // longitude of the node
        this.lat = lat; // latitude of the node
    }

    double lng;
    double lat;
    double gscore; // node's gscore, also equal to g(node)
    Node parent; // set node.parent attribute


    /**
     * This function describes the Manhattan distance between two nodes
     *
     * @param a one Node a
     * @param b another Node b
     * @return the Manhattan distance between a and b
     */
    public static double ManDistance(Node a, Node b) {
        double ans;
        ans = Math.abs(a.lat - b.lat) + Math.abs(a.lng - b.lng);
        return ans;
    }

    /**
     * This function describes the euclidean distance between two nodes
     *
     * @param a Node a
     * @param b Node b
     * @return euclidean distance between two nodes
     */
    public static double EucDistance(Node a, Node b) {
        double ans;
        ans = Math.sqrt(Math.pow((a.lat - b.lat), 2) + Math.pow((a.lng - b.lng), 2));
        return ans;
    }

    /**
     * This function is used to judge if node a is in priority queue b
     *
     * @param a Node a
     * @param b Priority Queue b
     * @return if node a in queue b, return true, else return false
     */
    public static boolean inqueue(Node a, PriorityQueue<Node> b) {
        for (Node vn : b) {
            if (vn.lat == a.lat && vn.lng == a.lng) {
                return true;
            }
        }
        return false;
    }


    /**
     * This is the function called AStar, which is used to realize A* algorithm
     *
     * @param p             list of polygons
     * @param cur           current node
     * @param nxt           next node
     * @param openlist_size the openlist size that we choose
     * @return the list of answer nodes
     */
    public static List<Node> AStar(List<Polygon> p, Node cur, Node nxt, int openlist_size) {

        Comparator<Node> cNode = (o1, o2) -> {
            if (Node.EucDistance(o1, nxt) + o1.gscore == Node.EucDistance(o2, nxt) + o2.gscore) {
                return 0;
            }
            return Node.EucDistance(o1, nxt) + o1.gscore < Node.EucDistance(o2, nxt) + o2.gscore ? -1 : 1;
        }; // a comparator for the priority queue with lambda expression
        // choose the one with the smallest f score, which is g(n) + h(n), here h(n) is the euclidean distance
        //  You can also choose manhattan distance to generate other solutions.

        PriorityQueue<Node> openlist = new PriorityQueue<>(cNode); // initialize openlist
        PriorityQueue<Node> closelist = new PriorityQueue<>(cNode);// initialize closelist
        List<Node> anslist = new ArrayList<>();                    // initialize anslist
        cur.parent = null;  // initialize the first node's father to null
        cur.gscore = 0; //initialize the first node's g(n) to 0
        List<Point> routeCoordinates = new ArrayList<>(); // initialize routeCoordinates
        // This list of points is used to generate a Line2D object
        routeCoordinates.add(Point.fromLngLat(cur.lng, cur.lat)); // add the first point

        // First judge if cur and nxt is smaller than 0.0002:
        // If so, return

        if (Node.EucDistance(cur, nxt) < 0.0002) {  // if dis(cur, nxt) < 0.0002
            Node new_node = new Node(0.0, 0.0);

            // find the point with no intersection with illegal areas
            for (int i = 0; i < 36; i++) {
                new_node.lat = cur.lat + 0.0003 * Math.sin(i * 10 * Math.PI / 180);
                new_node.lng = cur.lng + 0.0003 * Math.cos(i * 10 * Math.PI / 180);
                new_node.parent = cur; // generate new_node 's lat and lng and parent node
                var a = Point.fromLngLat(new_node.lng, new_node.lat);
                routeCoordinates.add(a);
                LineString lineString = LineString.fromLngLats(routeCoordinates); // add the new node to the point list
                if (!BlockArea.LineCrossPolygon(p, lineString)) { // if legal path
                    openlist.add(new_node); // add the new node to the openlist
                }
                routeCoordinates.remove(a); // remove the current new node from the list, get the next judgement
            }
            anslist.add(openlist.poll()); // add the first element in the queue to the openlist
            anslist.add(cur); // add the starting point
            return anslist; // return the answer list
        }

        // if dis(cur,nxt) > 0.0002
        openlist.add(cur);  // add cur to the openlist list
        Node sp = cur; // set a optimal point, which is initialized with starting point
        while (!openlist.isEmpty()) { // if openlist is not empty
            Node n = openlist.poll(); // The first element of priority queue
            if (Node.ManDistance(sp, nxt) > Node.ManDistance(n, nxt)
                    || Node.EucDistance(sp, nxt) > Node.EucDistance(n, nxt) || sp.gscore <= n.gscore) {
                sp = n; // if n is optimal, set sp as n
            }
            if (Node.EucDistance(n, nxt) < 0.0002) { // if in distance after iterations
                while (n != null) {
                    anslist.add(n);
                    n = n.parent;
                }  // add n itself and its parent node iteratively until reaching the starting node
                return anslist;
            } else {
                closelist.add(n); // if not in range
                if (openlist.size() > openlist_size) { // if reach the maximum openlist size
                    while (sp != null) {
                        anslist.add(sp);
                        sp = sp.parent;
                    }
                    // add sp itself and its parent node iteratively until reaching the starting node
                    return anslist;
                }
                // if not reach the maximum openlist size
                for (int i = 0; i < 36; i++) {
                    Node new_node = new Node(0.0, 0.0);
                    new_node.lat = n.lat + 0.0003 * Math.sin(i * 10 * Math.PI / 180);
                    new_node.lng = n.lng + 0.0003 * Math.cos(i * 10 * Math.PI / 180);
                    new_node.parent = n;  // set new node
                    if (inqueue(new_node, closelist)) {// if new node in close list, pass
                        continue;
                    }
                    if (!inqueue(new_node, openlist)) {// if new node not in close list and openlist

                        new_node.gscore = n.gscore + 0.0003; // g(new_node) = g(n) + 0.0003;
                        var a = Point.fromLngLat(new_node.lng, new_node.lat);
                        routeCoordinates.add(a);
                        LineString lineString = LineString.fromLngLats(routeCoordinates);

                        if (!BlockArea.LineCrossPolygon(p, lineString)) {// if legal, add to openlist
                            openlist.add(new_node);
                        }
                        routeCoordinates.remove(a);
                    } else if (inqueue(new_node, openlist)) {// if already in openlist
                        for (Node a : openlist) {
                            if (a.lat == new_node.lat && a.lng == new_node.lng) {//find the node
                                List<Point> rc = new ArrayList<>();
                                rc.add(Point.fromLngLat(cur.lng, cur.lat));
                                rc.add(Point.fromLngLat(new_node.lng, new_node.lat));
                                LineString ls = LineString.fromLngLats(rc);
                                if (a.gscore > new_node.gscore && !BlockArea.LineCrossPolygon(p, ls)) { // if new_node has a lower gscore
                                    // then substitute it with new gscore
                                    a.gscore = new_node.gscore;
                                    a.parent = new_node.parent;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null; // if not successful, return null.
        // Normally the function will not execute at this point.
    }


    /**
     * This function will calculate the angle after the A* algorithm reaches its end.
     * We certify that A* algorithm gives us the right answer.
     *
     * @param a point a
     * @param b point b
     * @return the angle from a to b (vector a -> b)
     */
    public static double CountAngle(Point a, Point b) { // Calculate the angle when Moving a to b

        double ans = 0;
        double tmp;

        //Total 10 conditions with no repetition
        if (a.latitude() == b.latitude()) { // a and b have the same latitude, three conditions
            if (a.longitude() < b.longitude()) {
                ans = 0;
            } else if (a.longitude() > b.longitude()) {
                ans = 180;
            } else if (a.longitude() == b.longitude()) {
                System.out.println("You could not move to the Node itself");
                return -1;
            }
        }
        if (a.longitude() == b.longitude()) {// a and b have the same longitude, three conditions
            if (a.latitude() < b.latitude()) {
                ans = 90;
            } else if (a.latitude() > b.latitude()) {
                ans = 270;
            } else if (a.latitude() == b.latitude()) {
                System.out.println("You could not move to the Node itself");
                return -1;
            }
        }

        if (a.longitude() < b.longitude()) { // a has a smaller longitude ,two conditions

            if (a.latitude() > b.latitude()) {
                tmp = (b.latitude() - a.latitude()) / (b.longitude() - a.longitude());
                ans = (360.0 + Math.atan(tmp) * 180.0 / Math.PI);
            }
            if (a.latitude() < b.latitude()) {
                tmp = (b.latitude() - a.latitude()) / (b.longitude() - a.longitude());
                ans = (Math.atan(tmp) * 180.0 / Math.PI);
            }
        }

        if (a.longitude() > b.longitude()) { // a has a larger longitude ,two conditions
            if (a.latitude() > b.latitude()) {
                tmp = (b.latitude() - a.latitude()) / (b.longitude() - a.longitude());
                ans = (180.0 + Math.atan(tmp) * 180.0 / Math.PI);
            }
            if (a.latitude() < b.latitude()) {
                tmp = (b.latitude() - a.latitude()) / (b.longitude() - a.longitude());
                ans = (180.0 + Math.atan(tmp) * 180 / Math.PI);
            }

        }
        return ans;
    }
}
