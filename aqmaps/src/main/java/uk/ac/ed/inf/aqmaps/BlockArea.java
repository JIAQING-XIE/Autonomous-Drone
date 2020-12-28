package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Polygon;

/**
 * This class is to define list of polygons that is illegal.
 * Illegal areas include the no-fly-zones and the area outside confinement area.
 */
public class BlockArea {

    /**
     * This function is used to create a list of polygons that is no-fly-zone area
     *
     * @return list of polygons
     * @throws IOException          ioexception
     * @throws InterruptedException interrupted exception
     */
    public static List<Polygon> Polygons() throws IOException, InterruptedException {
        String blockarea = ParseURL.Buildings2Str();
        String ba = ParseURL.ReadFile(blockarea); // parse the no-fly-zone.geojson
        FeatureCollection fc = FeatureCollection.fromJson(ba); //turn the geojson strings to feature collection objects
        List<Feature> f = fc.features(); // initialize list of features
        List<Polygon> pp = new ArrayList<>(); // initialize list of polygons
        assert f != null;
        for (Feature feature : f) { // for each feature in the feature collection
            Geometry g = feature.geometry(); // geometrical feature
            pp.add((Polygon) g); // turn geometrical feature to Polygon feature and add to list
        }
        return pp; // return the polygon list
    }

    /**
     * This function is used to judge if a line intersects the polygon
     *
     * @param p  list of polygons
     * @param ls line string type
     * @return if a line intersects the polygon, return true, else return false
     */
    public static boolean LineCrossPolygon(List<Polygon> p, LineString ls) {

        // Turn the intersection of line and Polygon
        // to the problem of intersection of lines and lines
        boolean ans; // initialize ans with false;
        var c = ls.coordinates();
        Line2D myline = new Line2D.Double(c.get(0).longitude(), c.get(0).latitude(), c.get(1).longitude(),
                c.get(1).latitude()); // create a line
        for (Polygon polygon : p) {
            double cur_lng; //  initialize current node longitude
            double cur_lat; //  initialize current node latitude
            double nxt_lng; //  initialize next node longitude
            double nxt_lat; //  initialize next node latitude
            var tmp = polygon.coordinates(); /// The polygon i
            for (int j = 0; j < tmp.get(0).size() - 1; j++) {
                cur_lat = tmp.get(0).get(j).latitude(); //  get current node latitude
                cur_lng = tmp.get(0).get(j).longitude();//  get current node longitude
                nxt_lat = tmp.get(0).get(j + 1).latitude();//  get next node latitude
                nxt_lng = tmp.get(0).get(j + 1).longitude();//  get next node longitude
                ans = myline.intersectsLine(cur_lng, cur_lat, nxt_lng, nxt_lat); // use internal method to judge if the line
                // intersects the polygon edges
                if (ans) { // if intersects, return yes
                    return true;
                }
            }
        }
        return false; // not intersects with all polygon edges, return false
    }

}
