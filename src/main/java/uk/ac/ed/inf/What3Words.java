package uk.ac.ed.inf;

/**
 * Represents the details related to each What3Words entry on the server,
 * including the location, the location of the nearest place, the words,
 * language and country.
 */
public class What3Words {
    /** the country it is located in */
    private String country;
    /** a list containing the longitude and latitude details of this square */
    private Square square;
    /** a String containing the nearest place to this point */
    private String nearestPlace;
    /** a pair of coordinates given in longitude and latitude */
    private LngLat coordinates;
    /** the 3 words associated with this location */
    private String words;
    /** the language these words are written in */
    private String language;
    /** a link leading to the map of this location */
    private String map;

    /**
     * @return a LngLat containing the coordinates of this location
     */
    public LngLat getCoordinates() {
        return coordinates;
    }

    /**
     * Represents the coordinates which are used to define this location as a square.
     */
    public static class Square{
        /** the coordinates of the southwest corner of this square */
        private LngLat southwest;
        /** the coordinates of the northeast corner of this square */
        private LngLat northeast;
    }

    /**
     * Represents a pair of coordinates as a latitude and longitude.
     */
    public static class LngLat{
        /** the longitude of this location */
        private double lng;
        /** the latitude of this location */
        private double lat;

        /**
         * @return a double with the longitude of this location
         */
        public double getLng() {
            return lng;
        }

        /**
         * @return a double with the latitude of this location
         */
        public double getLat() {
            return lat;
        }
    }
}
