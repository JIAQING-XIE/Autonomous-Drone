package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This class is the constructor, which is used for parsing the air-quality.json file
 * It will split into key-value pairs.
 */
public class Record {
    String location; // key : location
    double battery;  // key : battery
    String reading;  // key : reading

    static Type listType = new TypeToken<ArrayList<Record>>() {
    }.getType();  // return the type of attributes in Record

    /**
     * This function is used to return array of locations, batteries or readings
     * ,according to the array that we want.
     *
     * @param s, s refers to jsonString
     * @param k, k refers to "key", which is used to infer attribute type here.
     * @return array of location, battery or reading
     */
    public static ArrayList<?> get_Arr(String s, String k) {
        ArrayList<Record> recordlist = new Gson().fromJson(s, listType); // get the Record object(array)
        ArrayList<String> string_arr = new ArrayList<>(); // initialize for location or reading
        ArrayList<Double> double_arr = new ArrayList<>(); // initialize for battery
        switch (k) {
            case "location":
                for (Record record : recordlist) {
                    string_arr.add(record.location); // if key = location, add to string_arr
                }
                break;
            case "battery":
                for (Record record : recordlist) {
                    double_arr.add(record.battery); // if key = battery, add to double_arr
                }
                break;
            case "reading":
                for (Record record : recordlist) {
                    string_arr.add(record.reading); // if key = reading, add to string_arr
                }
                break;
            default:
                System.out.println("Unknown Record data members"); // Print error information on the screen

                break;
        }
        return k.equals("location") || k.equals("reading") ? string_arr : double_arr; // return string_arr if key is equal to location or reading
    }

}
