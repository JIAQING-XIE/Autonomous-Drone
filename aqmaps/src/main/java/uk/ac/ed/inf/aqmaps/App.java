package uk.ac.ed.inf.aqmaps;

import static java.lang.Double.parseDouble;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * App
 */
public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        ParseURL.port = args[6]; // get the port from the command line
        @SuppressWarnings("unused")
        int random_seed = Integer.parseInt(args[5]); // get the random seed from command line
        // we do not use random_seed in this project, but it should be read from command
        // line
        String maps = ParseURL.Maps2Str(args[0], args[1], args[2]);// Get specific maps from the given date
        String maps_string = ParseURL.ReadFile(maps); // Get the string from json file

        var reading = Record.get_Arr(maps_string, "reading"); // get the reading
        var location = Record.get_Arr(maps_string, "location"); // get the location
        var battery = Record.get_Arr(maps_string, "battery"); // get the battery
        int min_steps = 150; // initialized by upper limit : 150 steps

        List<List<Point>> best_pointList = new ArrayList<>(); // Best answer_list with minimum steps
        List<Polygon> pol = BlockArea.Polygons(); // non-fly polygon list
        Polygon big = Display.poly(55.942617, 55.946233, -3.192473, -3.184319);

        pol.add(big); // we should also add the confinement area as the polygon to get its edges(line
        // strings)

        List<Node> Record_copy = new ArrayList<>();
        Node cur_copy = new Node(parseDouble(args[4]), parseDouble(args[3])); // initial point

        List<Feature> featureList = new ArrayList<>(); // This feature list will further become FeatureCollection
        // List<Feature> featureList_optimal = new ArrayList<>();
        for (int i = 0; i < location.size(); i++) {
            // System.out.println("\n");
            var details = new Gson().fromJson(ParseURL.ReadFile(ParseURL.Words2Str((String) location.get(i))),
                    WordDetails.class);
            Node s = new Node(details.coordinates.lng, details.coordinates.lat);
            Record_copy.add(s); // make a copy of sensors, since each openlist_size loop will drop original
            // record.

            featureList.add(Display.SensorFeature(details.coordinates.lng, details.coordinates.lat,
                    (String) reading.get(i), (double) battery.get(i), (String) location.get(i))); // add feature properties

            if (i == 32) {
                featureList.add(Display.SensorFeature(details.coordinates.lng, details.coordinates.lat,
                        (String) reading.get(i), (double) battery.get(i), (String) location.get(i)));
                // we should add the last sensor information to avoid being deleted during the process
                // quit
            }

        }

        /*------ Begin AStar Algorithm --------*/


        int[] openlist_SIZE = new int[]{36, 72, 108, 144, 100, 120, 150, 180, 200, 250, 300, 400, 1000, 1200, 2000, 2400, 3000, 3600, 5400, 6000}; // 20 values for openlist size , we just choose
        // the multiple of 10 or 36(number of directions), You can also choose other sizes for try

        for (int openlist_size : openlist_SIZE) {
            // Useful local variables
            AtomicInteger cnt = new AtomicInteger(); // count minimum steps
            AtomicReference<Node> cur = new AtomicReference<>(new Node(cur_copy.lng, cur_copy.lat)); // current node
            AtomicReference<Node> nxt = new AtomicReference<>(new Node(0, 0)); // Aim sensor(node)
            List<String> sensors = new ArrayList<>(); // record sensors : what3words location or null if not in range
            @SuppressWarnings("unchecked")
            List<String> location_copy = new ArrayList<>((Collection<? extends String>) location); // a copy for location
            //location information needs to be used each loop, reserved
            List<Double> angles = new ArrayList<>(); // record the angles of each path
            List<Point> Ans_point = new ArrayList<>(); // record the current list of points to be visited
            List<Node> Record_Node = new ArrayList<>(Record_copy); // record information needs to be used each loop, reserved

            double mini = 1; // works as the infinite, compared with the value less than 0.01
            /*---------get the first aim sensor----------*/
            for (Node node : Record_copy) {
                Node nn = new Node(0, 0);
                mini = getMin(cur, nxt.get(), nn, mini, node);
            }
            /* A* search might not be ended in some seconds, interrupt it
            since the path length will obviously overpass 150,
            therefore we set the thread to deal with it */
            var t = new Thread(() -> {
                try {
                    boolean move = false; // not move (at the first step)
                    while (!Record_Node.isEmpty()) { // if at least one sensor has not been visited
                        Node nxtt = new Node(0, 0);
                        double min = 1; // just work for function, return the distance from current node
                        // to the nearest node
                        // Situation 1, the aim sensor is in range
                        if (Record_Node.size() == 33) { // visit the first node, important issue
                            int mark = 0;
                            if (Node.EucDistance(cur.get(), nxt.get()) < 0.0002 && !move) { // not move and also in range
                                get_answer(pol, openlist_size, cnt, cur, nxt.get(), Ans_point, sensors); // perform A* once
                                // if first in range, the one step out and back to read the value
                                Point point1 = Point.fromLngLat(cur.get().lng, cur.get().lat);
                                Point point2 = Point.fromLngLat(Double.parseDouble(args[4]),
                                        Double.parseDouble(args[3]));
                                Ans_point.add(point1);
                                Ans_point.add(point2); // Add to ans_point list
                                // The idea is that first move to a point, then came back to the initial point
                                // For example, from (1,2)-> (2,1)->(1,2), then the sensors list will be [null, sensor_name]
                                // then pass and restart with initial point(1,2)

                                cnt.getAndIncrement(); // path length = path length + 1
                                mark = 1; //( in range mark)
                                move = true;
                            } else {
                                // not in range
                                while (Node.EucDistance(cur.get(), nxt.get()) > 0.0002) {
                                    get_answer(pol, openlist_size, cnt, cur, nxt.get(), Ans_point, sensors);
                                    // perform A* until in range
                                    move = true;
                                }
                                sensors.remove(sensors.size() - 1); // move the last null, prepare for adding the what3words location
                            }
                            // add the what3words location, move it from the record list
                            for (int i = 0; i < Record_Node.size(); i++) {
                                if (Record_Node.get(i).lat == nxt.get().lat
                                        && Record_Node.get(i).lng == nxt.get().lng) {
                                    Record_Node.remove(i);
                                    sensors.add(location_copy.get(i));
                                    location_copy.remove(i);
                                    break;
                                }
                            }
                            // if first node in range, add null to the first move
                            if (mark == 1)
                                sensors.add(null);
                            for (Node node : Record_Node) {
                                min = getMin(cur, nxt.get(), nxtt, min, node);
                            }
                            move = false;
                        }
                        if (Node.EucDistance(cur.get(), nxt.get()) < 0.0002) {//
                            /* if not move, then move one step, set boolean sign move as true */
                            if (!move) {//
                                get_answer(pol, openlist_size, cnt, cur, nxt.get(), Ans_point, sensors);
                                move = true;
                                continue;
                            }
                            /*
                             * if move, then record the current sensor and find next sensor, set move as
                             * false
                             */
                            else {
                                sensors.remove(sensors.size() - 1);
                                for (int i = 0; i < Record_Node.size(); i++) {
                                    if (Record_Node.get(i).lat == nxt.get().lat
                                            && Record_Node.get(i).lng == nxt.get().lng) {
                                        Record_Node.remove(i);
                                        sensors.add(location_copy.get(i)); // add the current what3words location
                                        location_copy.remove(i);// remove the current sensor(visited)
                                        break;
                                    }
                                }
                                //Find the next sensor to be visited in range
                                for (Node node : Record_Node) {
                                    min = getMin(cur, nxt.get(), nxtt, min, node);
                                }
                                move = false; // set move to false
                            }
                            continue;
                        }
                        /*
                         * if not in range, then set sign as true, move!
                         */
                        get_answer(pol, openlist_size, cnt, cur, nxt.get(), Ans_point, sensors);
                        move = true;
                    }
                    Thread.sleep(0);
                } catch (InterruptedException ignored) {
                }
            });
            t.start();
            try {
                t.join(2000); // limit time : 2000ms
            } catch (InterruptedException e) {
                t.interrupt(); // if exceeds time, then interrupt
                continue;
            }
            // The program may not address the problem since the A* search needs lots of
            // tries and time. Commonly,
            // it will end in 2-4s, if not stop, then Record_Node will not be empty, then
            // try a different openlist
            if (!Record_Node.isEmpty()) {
                continue;
            }
            // Here it seems that the A* algorithm addressed the problem successfully
            // The only thing that we need to do is to move the last point to the original
            // point(nearby).
            // If already nearby, then move to return total length and compare with the
            // optimal minimum total stpes
            Node init = new Node(Ans_point.get(0).longitude(), Ans_point.get(0).latitude());
            AtomicReference<Node> last = new AtomicReference<>(new Node(Ans_point.get(Ans_point.size() - 1).longitude(),
                    Ans_point.get(Ans_point.size() - 1).latitude()));
            // Here we need a new thread too.
            var t2 = new Thread(() -> {
                try {
                    while (Node.EucDistance(last.get(), init) > 0.0002) {
                        get_answer(pol, openlist_size, cnt, last, init, Ans_point, sensors);
                    }

                    Thread.sleep(0);
                } catch (InterruptedException ignored) {
                } // end of try-catch block
            });
            t2.start();
            try {
                t2.join(2000);
            } catch (InterruptedException e) {
                t2.interrupt();
            } // end of thread t2

            // Pass the test that it doesn't have intersections with illegal areas' edges
            if (Node.EucDistance(last.get(), init) <= 0.0002) {
                boolean success = true;
                for (int i = 0; i < Ans_point.size() - 1; i++) {
                    List<Point> rc = new ArrayList<>();
                    rc.add(Point.fromLngLat(Ans_point.get(i).longitude(), Ans_point.get(i).latitude()));
                    rc.add(Point.fromLngLat(Ans_point.get(i + 1).longitude(), Ans_point.get(i + 1).latitude()));
                    LineString ls = LineString.fromLngLats(rc);
                    if (BlockArea.LineCrossPolygon(pol, ls)) {
                        success = false;
                    }
                }

                if (cnt.get() < min_steps && success) {//
                    // Congratulations, we have passed the tests and get the current minimum
                    // steps
                    min_steps = cnt.get(); // current minimum steps
                    best_pointList.clear(); // clear the best point lists
                    best_pointList.add(Ans_point); // add answer lists to the best point lists

                    // get the answer points with no repeat and in order.
                    // List<Point> ans_points
                    for (int i = 0; i < Ans_point.size() - 1; ++i) {
                        if (Ans_point.get(i).longitude() == Ans_point.get(i + 1).longitude()
                                && Ans_point.get(i).latitude() == Ans_point.get(i + 1).latitude()) {
                            Ans_point.remove(i + 1);
                        }
                        angles.add(Node.CountAngle(Ans_point.get(i), Ans_point.get(i + 1)));
                    }
                    // add angles
                    MultiLineString multiLineStringFromLngLat;
                    multiLineStringFromLngLat = MultiLineString.fromLngLats(best_pointList);
                    if (featureList.size() > 1)
                        featureList.remove(featureList.size() - 1);
                    featureList.add(Feature.fromGeometry(multiLineStringFromLngLat)); // add the list of line string features
                    var featureCollection = FeatureCollection.fromFeatures(featureList);// change the featurelist to feature collection

                    /*---------- Output Stream ----------*/
                    System.out.println("Current Minimum steps");
                    System.out.println(cnt.get());
                    /*---------- Write to Geojson File ----------*/
                    String standard_json = FileGenerator.formatJson(featureCollection.toJson());
                    FileWriter file = new FileWriter(
                            "readings-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson");
                    FileGenerator.WriteToGeojson(file, standard_json);
                    /*---------- Write to flightPath file -----------*/
                    FileWriter pathfile = new FileWriter(
                            "flightpath-" + args[0] + "-" + args[1] + "-" + args[2] + ".txt");
                    FileGenerator.WriteToFlightPath(pathfile, Ans_point, angles, sensors);
                    System.out
                            .println("readings-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson file generated");
                    // Add LineString for the Best Ans_point;
                    // break;
                }
            }
        } // end of for loop

        System.exit(0);
    }

    /**
     * This function is used to get the nearest sensor from the current node
     *
     * @param cur  current Node
     * @param nxt  Next Node
     * @param nxtt a local variable
     * @param min  local minimum value
     * @param node Node node, which is the sensor's information in node
     * @return
     */
    private static double getMin(AtomicReference<Node> cur, Node nxt, Node nxtt, double min, Node node) {
        nxtt.lat = node.lat;
        nxtt.lng = node.lng;
        if (Node.ManDistance(cur.get(), nxtt) < min) { // if find a nearer sensor
            min = Node.ManDistance(cur.get(), nxtt); // refresh the minimum value
            nxt.lat = nxtt.lat; //next sensor's latitude
            nxt.lng = nxtt.lng;//next sensor's longitude
        }
        return min; // return the minimum value
    }

    /**
     * This function will add points to ans_point list, as well as add steps and add null to the sensors
     *
     * @param pol           the list of polygons
     * @param openlist_size openlist size
     * @param cnt           sum of minimum step
     * @param cur           current node
     * @param nxt           next node
     * @param ans_point     answer point lists
     * @param sensors       null or the what3words location name
     */
    private static void get_answer(List<Polygon> pol, int openlist_size, AtomicInteger cnt, AtomicReference<Node> cur,
                                   Node nxt, List<Point> ans_point, List<String> sensors) {
        List<Node> ans = Node.AStar(pol, cur.get(), nxt, openlist_size);
        assert ans != null;
        for (int i = ans.size() - 1; i >= 0; i--) {
            Point point = Point.fromLngLat(ans.get(i).lng, ans.get(i).lat);
            ans_point.add(point);
            cnt.getAndIncrement();
            sensors.add("null"); // not in the range of 0.0002, add the null
        }
        sensors.remove(sensors.size() - 1); // one redundant null, remove it

        cnt.getAndDecrement(); //  steps = steps + 1
        cur.set(ans.get(0)); // change the current point
    }
}
