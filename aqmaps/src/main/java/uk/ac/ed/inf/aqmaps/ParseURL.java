package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * This class serves to parse url addresses and get json strings from the files
 */
public class ParseURL {
    private static final String base_url = "http://localhost:"; // The front of url which can not be modified
    public static String port = "80"; // Default port is 80, but can be changed by command line parameter args[6]
    private static final String maps_name = "air-quality-data.json";    // filename under maps folder
    private static final String words_name = "details.json";            // filename under words folder
    private static final String buildings_name = "no-fly-zones.geojson";// filename under buildings folder

    /**
     * This function is used to parse url addresses with URI.
     *
     * @param s s represents string
     * @return the response content
     * @throws IOException          interrupt when meeting io exceptions
     * @throws InterruptedException interrupt exceptions
     */
    public static String ReadFile(String s) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient(); // call a client
        var request = HttpRequest.newBuilder().uri(URI.create(s)).build(); // make a request
        var response = client.send(request, BodyHandlers.ofString()); // get the repsonse
        return response.body(); // get the reponse body
    }

    /**
     * This function is used to construct the url address under maps folder
     *
     * @param dd dd is day
     * @param mm mm is month
     * @param yy yy is year
     * @return the url address of the file under maps folder
     */
    public static String Maps2Str(String dd, String mm, String yy) {
        return base_url + port + "/maps" + "/" + yy + "/" + mm + "/" + dd + "/" + maps_name;
    }

    /**
     * This function is used to construct the url address under words folder
     *
     * @param s the name of a what3words location
     * @return the url address of the file under words folder
     */
    public static String Words2Str(String s) {
        String[] str = s.split("\\.");
        return base_url + port + "/words" + "/" + str[0] + "/" + str[1] + "/" + str[2] + "/" + words_name;
    }

    /**
     * This function is used to construct the url address under buildings folder
     *
     * @return the url address of the file under buildings folder
     */
    public static String Buildings2Str() {
        return base_url + port + "/buildings" + "/" + buildings_name;
    }
}
