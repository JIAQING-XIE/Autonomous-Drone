package uk.ac.ed.inf.aqmaps;


/**
 * This class is a constructor for parsing json files(details.json)
 * Specifically, we only parse the key coordinates and child keys lng and lat,
 * which are longitude and latitude. Other keys are not used, so we do not consider adding them.
 */
public class WordDetails {
    Coor coordinates;

    public static class Coor {
        // two attributes follow attribute coordinates
        double lng;
        double lat;
    }
}
