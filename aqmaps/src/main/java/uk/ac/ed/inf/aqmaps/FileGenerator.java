package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This class is used for generating files for further check and upload.
 */
public class FileGenerator {

    /**
     * This function is used to write to flight path files
     *
     * @param pathfile  a new file to be written into
     * @param pointList list of points
     * @param angles    list of angles
     * @param sensors   list of sensors with null or what3words location
     * @throws IOException throw the exception if meet io exception
     */
    public static void WriteToFlightPath(FileWriter pathfile, List<Point> pointList, List<Double> angles, List<String> sensors) throws IOException {
        for (int i = 0; i < pointList.size() - 1; i++) {
            String clon = String.valueOf(pointList.get(i).longitude());  // longitude of current node
            String clat = String.valueOf(pointList.get(i).latitude()); // longitude of current node
            String nlon = String.valueOf(pointList.get(i + 1).longitude()); // latitude of next node
            String nlat = String.valueOf(pointList.get(i + 1).latitude()); // latitude of next node
            // int, double, double, int, double, double, string
            pathfile.write("" + (i + 1) + "," + clon + "," + clat + "," + Math.round(angles.get(i)) + "," + nlon + "," + nlat + "," + sensors.get(i));
            // "\n"
            pathfile.write("\n");
        }
        pathfile.close(); // finish writing, close the file
    }


    /**
     * This function is used to write to geojson files
     *
     * @param file    geojson file to be written into
     * @param jsonStr geojson strings
     * @throws IOException throw out an exception if encounter an ioexception.
     */
    public static void WriteToGeojson(FileWriter file, String jsonStr) throws IOException {
        file.write(jsonStr); // write to the file writer
        file.close(); // finish writing, close the file
    }

    /**
     * This function is defined in my heatmap project and I just copy them for further use
     * It is used for formatting geojson strings
     *
     * @param jsonStr geojson strings
     * @return the formatted geojson file
     */
    public static String formatJson(String jsonStr) {
        StringBuilder sb = new StringBuilder(); // String for building json string blocks
        char current; // Initialize last, current
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            current = jsonStr.charAt(i);
            switch (current) {
                case '{': // meeting '{' and '[' will move to the next line and call addIndent function
                case '[':
                    sb.append(current); // add '[' to the sb
                    sb.append('\n'); // add '\n', move to the next line
                    indent++; // one indent block: two blank space
                    addIndent(sb, indent); // call addIndent function
                    break;
                case '}':// meeting '}' and ']' will move to the next line and call addIndent function
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndent(sb, indent);// align with the last '{' and '['
                    sb.append(current);// delete indent
                    break;
                case ',': // perform similarly like ']' but align with the next line
                    sb.append(current);
                    sb.append('\n');
                    addIndent(sb, indent);
                    break;
                case ':':
                    sb.append(current).append(" "); // add one blank space after ":"
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString(); // Return the formatted json string

    }


    /**
     * This function is used for adding blank space
     *
     * @param sb a string builder
     * @param indent the length of indent be added to the front
     */
    private static void addIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');// append one blank space
            sb.append(' ');// append another blank space
        }
    }

}
