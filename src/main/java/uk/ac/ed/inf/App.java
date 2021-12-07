package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The main class of the drone application. Contains the necessary instances of
 * the WebServer, Database, and Path, and calls the functions to conduct the drone
 * operation, as well as creating the output files.
 */
public class App {
    /**
     * The entry point of application. Gets the orders from the database, and passes
     * these to the Path class to get the list of orders which are successfully delivered,
     * as well as the GeoJSON features of the flightpath. It then calls the methods to write
     * the GeoJSON string to a file, and write both the flightpath and the list of delivered
     * orders to their respective tables in the database.
     *
     * @param args the input arguments from the command line
     */
    public static void main(String[] args) {
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String databasePort = args[4];

        WebServer server = new WebServer(webPort);
        Menus menu = new Menus(server);
        Database db = new Database(day, month, year, databasePort);
        ArrayList<Order> orders;
        ArrayList<Order> delivered;
        FeatureCollection pathFeatures;
        try {
            orders = db.getOrders(server);
            Path path = new Path(orders,server);
            delivered = path.generatePath(menu);
            pathFeatures = path.getPathFeatures();
            path.createMap(pathFeatures,day,month,year);
            db.writeOrders(delivered);
            db.writeFlightpath(path.getFlightpath());
        } catch (SQLException dbUnavailable) {
            System.err.println("Something went wrong when trying to communicate with database. Please try again.");
            System.exit(1);
        }
    }
}