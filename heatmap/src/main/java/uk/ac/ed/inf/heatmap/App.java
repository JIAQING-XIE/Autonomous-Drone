package uk.ac.ed.inf.heatmap;

/*
 * Import java.io and java.util
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Import java API : mapbox.geojson
 */
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class App {
    public static void main(String[] args) throws IOException {
	String[] numbersArray = txt2Str(args[0]);// read strings by commas from predicitons.txt
	/*-------   define the squared confinement area -------*/
	double polygon_lat[] = { 55.946233, 55.942617 }; // limited by two latitudes
	double polygon_lng[] = { -3.192473, -3.184319 }; // limited by two longitudes

	/*
	 * Default grid_segment_num is equal to 10 since this project serves default 10
	 * * 10 grid segmentation rule. The program is still robust when it meets
	 * conditions like 11 * 11 grids or 12 * 12 grids or even more requirements of
	 * grids segmentation rules.
	 * 
	 */
	int grid_segment_num = (int) Math.sqrt(numbersArray.length);
	double[] LA = new double[grid_segment_num + 1]; // new a double array for reserving latitudes
	double[] LO = new double[grid_segment_num + 1]; // new a double array for reserving longitudes

	for (int i = 0; i < grid_segment_num + 1; i++) {
	    LA[i] = polygon_lat[0] - i * (polygon_lat[0] - polygon_lat[1]) / grid_segment_num; // from North to South
	    LO[i] = polygon_lng[0] + i * (polygon_lng[1] - polygon_lng[0]) / grid_segment_num; // from West to East
	} // Arithmetic division of lines of longitude and latitude and reserve them in LO
	  // and LA

	List<Feature> featureList = new ArrayList<Feature>(); // new a list for reserving features
	int k = 0; // current place of grid
	for (int i = 0; i < grid_segment_num; i++) { // from west to east and from north to south
	    for (int j = 0; j < grid_segment_num; j++) {
		int x = Integer.parseInt(numbersArray[k]); // change type from string to integer
		Feature polygonFeatureJson = GeoJSON(LA[i], LA[i + 1], LO[j], LO[j + 1], x); // call GeoJSON function
		featureList.add(polygonFeatureJson); // add features from West to East, from North to South
		k = k + 1; // move to next grid
	    }
	}

	FeatureCollection featureCollection = FeatureCollection.fromFeatures(featureList); // Collect 10 * 10 Polygons'
											   // features
											   // in this project

	String a2 = featureCollection.toJson(); // Transform features to json String
	String a = formatJson(a2); // Format features to standardized json String instead of one line

	System.out.println("Writing into heatmap.geojson..."); // Output Stream : begin writing
	FileWriter file = new FileWriter("heatmap.geojson"); // Create a .geojson file
	try {
	    file.write(a); // Write standardized json string to .geojson file
	    file.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace(); // If failed, output IOExcepetion information for us to debug
	}
	System.out.println("heatmap.geojson is generated"); // If IOExpection doesn't appear, print the message
    }

    /**
     * This function is trying to read the input file from command line, and parse
     * each line of the file. Numbers in each line will be written into the array,
     * of which the type is "String".
     * 
     * @param filename The name of the input file
     * @return numbersArray The array that generated from the file, split by comma.
     * @throws IOException print i/o exception information if it appears in program.
     */
    public static String[] txt2Str(String filename) throws IOException {
	File f = new File(filename);
	BufferedReader readTxt = new BufferedReader(new FileReader(f)); // Read file sufficiently in the buffer
	String textLine = ""; // initialize parameter textline
	String str = ""; // initialize parameter str to reserve strings in series
	while ((textLine = readTxt.readLine()) != null) {
	    str += textLine + ","; // Add comma at the end of each line!
	}
	readTxt.close(); // Close the text document to
	String[] numbersArray = str.split(","); // parsed by comma. Researchers can change it to " " or "\t" according
						// to their original segmentation rule of each value.
	for (int i = 0; i < numbersArray.length; i++) {
	    numbersArray[i] = numbersArray[i].replace(" ", ""); // delete blank space in each string
	}
	return numbersArray;
    }

    /**
     * This function is called GeoJSON, which is of the type "Feature". It collects
     * four points of an instance rectangle in this project. It concatenates four
     * point features into an independent polygon feature. Besides, this function
     * considers the "fill-opacity" property and "fill" property that is based on
     * geojson API. Specifically, the "fill" property is based on the numbers' range
     * in the input predictions.txt file while "fill-opacity" is already specified
     * as "0.75" in this project.
     * 
     * We define the polygon in the clockwise order, starting from the left top to
     * right top, to right bottom, to the left bottom and back to the starting
     * point(left top).
     * 
     * @param la_1 first latitude
     * @param la_2 second latitude
     * @param lo_1 first longitude
     * @param lo_2 second longitude
     * @param num  air quality index in the defined area
     * @return the polygon feature
     */
    public static Feature GeoJSON(double la_1, double la_2, double lo_1, double lo_2, int num) {

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
	Polygon p = Polygon.fromLngLats(POINT2); // Create the polygon instance
	Feature p_feature = Feature.fromGeometry(p); // Turn the instance into a polygon feature
	p_feature.addNumberProperty("fill-opacity", 0.75);// Add the "fill-opacity" property. The default number is 0.75
	p_feature.addStringProperty("fill", num2RGB(num));// Add the "fill" property according to the AQI transformed
							  // by function num2RGB.

	return p_feature;
    }

    /**
     * This function is called num2RGB, which serves to turn the parsed numbers in
     * the predictions.txt into RGB value(String Type). When the number is in the
     * range[0,32), color in the area is green. When the number is in the
     * range[32,64), color in the area is medium green. When the number is in the
     * range[64,96), color in the area is light green. When the number is in the
     * range[96,128), color in the area is lime green. When the number is in the
     * range[128,160), color in the area is gold. When the number is in the
     * range[160,192), color in the area is orange. When the number is in the
     * range[192,224), color in the area is red/orange. When the number is in the
     * range[224,256), color in the area is red. When the number is not in any range
     * that mentioned before, color in the area is black. In this task, we don not
     * care about not visited area but we will add to the second task.
     * 
     * @param num air quality index in the defined area
     * @return RGB value according to the rule in the specification
     */
    public static String num2RGB(int num) {
	String RGB = "#000000";
	if (num < 0 && num > 256) {
	    RGB = "000000"; // "Black"
	} else if (num < 32) {
	    RGB = "#00ff00";// "Green"
	} else if (num < 64) {
	    RGB = "#40ff00";// "Medium Green"
	} else if (num < 96) {
	    RGB = "#80ff00";// "Light Green"
	} else if (num < 128) {
	    RGB = "#c0ff00";// "Lime Green"
	} else if (num < 160) {
	    RGB = "#ffc000";// "Gold"
	} else if (num < 192) {
	    RGB = "#ff8000";// "Orange"
	} else if (num < 224) {
	    RGB = "#ff4000";// "Red/Orange"
	} else {
	    RGB = "#ff0000";// "Red"
	}
	return RGB;
    }

    /**
     * Since toJson function represents the feature collection message in a line, it
     * might be difficult for the researchers to debug outputs. Therefore we can
     * transform it into formatted json file by applying formatJson function. This
     * is not necessary. So if researchers do not want to see it carefully, then
     * remove it from the main function.
     * 
     * @param jsonStr Robust for both formatted and unformatted json string
     * @return Formatted json string
     */
    public static String formatJson(String jsonStr) {
	StringBuilder sb = new StringBuilder(); // String for building json string blocks
	char current = '\0'; // Initialize last, current
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
		sb.append(current + " "); // add one blank space after ":"
		break;
	    default:
		sb.append(current);
	    }
	}

	return sb.toString(); // Return the formatted json string

    }

    /**
     * This function serves to add two blank spaces to satisfy the json file's
     * style.
     * 
     * 
     * @param sb
     * @param indent
     */
    private static void addIndent(StringBuilder sb, int indent) {
	for (int i = 0; i < indent; i++) {
	    sb.append(' ');// append one blank space
	    sb.append(' ');// append another blank space
	}
    }

}
