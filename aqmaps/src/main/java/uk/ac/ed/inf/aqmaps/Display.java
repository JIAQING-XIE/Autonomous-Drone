package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to add color and marker properties to the point features
 * Also defined two final lists
 */
public class Display {
    // RGB_string is private and final, which can not be modified
    // RGB_string has 9 values
    private static final String[] RGB_string = {"#00ff00", "#40ff00", "#80ff00", "#c0ff00", "#ffc000", "#ff8000", "#ff4000", "#ff0000", "#000000"};
    //Marker_symbol is private and final, which can not be modified
    //Marker_symbol has 3 values
    private static final String[] Marker_symbol = {"lighthouse", "danger", "cross"};

    /**
     * This class is used to add properties to the point features
     *
     * @param lng      longitude of the sensor
     * @param lat      latitude of the sensor
     * @param Reading  reading of the sensor
     * @param battery  battery of the sensor
     * @param location location of the sensor
     * @return point features (type of Feature)
     */
    public static Feature SensorFeature(double lng, double lat, String Reading, double battery, String location) {
        Feature pointFeature = Feature.fromGeometry(Point.fromLngLat(lng, lat));
        if (Reading.equals("NaN") || Reading.equals("null") || battery < 10) { // if reading == Nan or null or battery < 10
            pointFeature.addStringProperty("marker-symbol", Display.Marker_symbol[2]); // marker symbol = cross
            pointFeature.addStringProperty("rgb-string", Display.RGB_string[8]); //rbg_string = #000000(black)
            pointFeature.addStringProperty("marker-color", Display.RGB_string[8]); // marker color = #000000(black)
            pointFeature.addStringProperty("location", location); // location is the what3words location

        } else {
            // if reading != Nan and != null and battery > 10
            // Add string properties according to the range of the reading value
            pointFeature.addStringProperty("rgb-string", Display.marker_and_color(Reading, battery)[0]);
            pointFeature.addStringProperty("marker-symbol", Display.marker_and_color(Reading, battery)[1]);
            pointFeature.addStringProperty("marker-color", Display.marker_and_color(Reading, battery)[0]);
            pointFeature.addStringProperty("location", location);
        }
        return pointFeature; // return the filled point feature
    }


    /**
     * This function is to define the properties according to range of the reading value
     *
     * @param read_value    reading value of the sensor
     * @param battery_value
     * @return string lists of two properties, one for marker-color and marker-symbol
     */
    public static String[] marker_and_color(String read_value, Double battery_value) {
        String[] s = new String[2];
        double reading_value = Double.parseDouble(read_value); // turn the string type to double type
        if (reading_value < 0 && reading_value >= 256) {
            System.out.println("Not a valid number that readed from drone");
        } else if (reading_value < 32) {
            s[0] = RGB_string[0];       // color = '#00ff00'
            s[1] = Marker_symbol[0];    // symbol = 'lighthouse'
        } else if (reading_value < 64) {
            s[0] = RGB_string[1];       // color = '40ff00'
            s[1] = Marker_symbol[0];    // symbol = 'lighthouse'
        } else if (reading_value < 96) {
            s[0] = RGB_string[2];       // color = '80ff00'
            s[1] = Marker_symbol[0];    // symbol = 'lighthouse'
        } else if (reading_value < 128) {
            s[0] = RGB_string[3];       // color = 'c0ff00'
            s[1] = Marker_symbol[0];    // symbol = 'lighthouse'
        } else if (reading_value < 160) {
            s[0] = RGB_string[4];       // color = 'ffc000'
            s[1] = Marker_symbol[1];    // symbol = 'danger'
        } else if (reading_value < 192) {
            s[0] = RGB_string[5];       // color = 'ff8000'
            s[1] = Marker_symbol[1];    // symbol = 'danger'
        } else if (reading_value < 224) {
            s[0] = RGB_string[6];       // color = 'ff4000'
            s[1] = Marker_symbol[1];    // symbol = 'danger'
        } else if (reading_value < 256) {
            s[0] = RGB_string[7];       // color = 'ff0000'
            s[1] = Marker_symbol[1];    // symbol = 'danger'
        }
        return s; //return the answer string list
    }

    /**
     * This function is used to generate a polygon given four points with four coordinates
     *
     * @param la_1 smallest latitude
     * @param la_2 largest latitude
     * @param lo_1 smallest longitude
     * @param lo_2 largest longitude
     * @return the polygon
     */
    public static Polygon poly(double la_1, double la_2, double lo_1, double lo_2) {
        List<Point> POINT = new ArrayList<Point>(); // POINT is the attribute of list of points
        List<List<Point>> POINT2 = new ArrayList<List<Point>>(); // POINT2 is the attribute of list of point list,
        // where is the input of Polygon.fromLngLats()
        // Therefore we should
        // create an array to reserve it.

        Point point1 = Point.fromLngLat(lo_1, la_1); // First point
        Point point2 = Point.fromLngLat(lo_1, la_2); // Second point
        Point point3 = Point.fromLngLat(lo_2, la_2); // Third point
        Point point4 = Point.fromLngLat(lo_2, la_1); // Fourth point

        /* Add point to the point list */
        POINT.add(point1);
        POINT.add(point2);
        POINT.add(point3);
        POINT.add(point4);
        POINT.add(point1);
        POINT2.add(POINT);

        Polygon big = Polygon.fromLngLats(POINT2);
        return big; // return polygon
    }
}
