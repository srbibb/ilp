package uk.ac.ed.inf;

/**
 * Represents a point as longitude and latitude,
 * and contains the functions for calculating drone position.
 */
public class LongLat {

    /** The longitude of the represented point */
    public double longitude;
    /** The latitude of the represented point */
    public double latitude;

    /**
     * Constructor for LongLat class.
     * @param lon the longitude of the represented point
     * @param lat the latitude of the represented point
     */
    public LongLat(double lon, double lat) {
        longitude = lon;
        latitude = lat;
    }

    /**
     * Checks whether the point is between the longitude -3.184319 and -3.192473,
     * and that the latitude is between 55.942617 and 55.946233.
     * @return a boolean value which is true if the point is within the confinement
     * area and false otherwise
     */
    public boolean isConfined() {
        return (longitude < -3.184319 & longitude > -3.192473) & (latitude > 55.942617 & latitude < 55.946233);
    }

    /**
     * Calculates the distance between this point and the given point, using the
     * Pythagorean distance formula.
     * @param compareValue the given point
     * @return a double containing the distance between this point and the given point
     */
    public double distanceTo(LongLat compareValue){
        return Math.sqrt((longitude - compareValue.longitude) * (longitude - compareValue.longitude)
                + (latitude - compareValue.latitude) * (latitude - compareValue.latitude));
    }

    /**
     * Checks whether the given point is less than 0.00015 degrees of this point.
     * @param compareValue the given point
     * @return a boolean value which is true if the given point is within 0.00015
     * of this point and false otherwise
     */
    public boolean closeTo(LongLat compareValue){
        return (distanceTo(compareValue) < 0.00015);
    }

    /**
     * Calculates the point that the drone will next move to with the current point
     * and an angle.
     * @param angle an integer containing the given angle of movement
     * @return a new instance of LongLat containing the new point the drone will move to
     */
    public LongLat nextPosition(int angle){
        double newLongitude;
        double newLatitude;
        double moveLength = 0.00015;
        if (angle == -999) {
            return new LongLat(longitude, latitude);
        }

        newLongitude = longitude + (Math.cos(Math.toRadians(angle))*moveLength);
        newLatitude = latitude + (Math.sin(Math.toRadians(angle))*moveLength);

        return new LongLat(newLongitude, newLatitude);

    }

}

