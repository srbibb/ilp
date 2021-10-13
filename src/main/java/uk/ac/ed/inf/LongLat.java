package uk.ac.ed.inf;

public class LongLat {
    public double longitude;
    public double latitude;

    public LongLat(double lon, double lat) {
        longitude = lon;
        latitude = lat;
    }

    public boolean isConfined() {
        return (longitude < -3.184319 & longitude > -3.192473) & (latitude > 55.942617 & latitude < 55.946233);
    }

    public double distanceTo(LongLat compareValue){
        return Math.sqrt((longitude - compareValue.longitude) * (longitude - compareValue.longitude)
                + (latitude - compareValue.latitude) * (latitude - compareValue.latitude));
    }

    public boolean closeTo(LongLat compareValue){
        return (distanceTo(compareValue) < 0.00015);
    }

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
    // public static void main(String[] args) {}

}

