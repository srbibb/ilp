package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;

import java.sql.SQLException;
import java.util.ArrayList;

public class App {
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
            Output.createMap(pathFeatures,day,month,year);
            db.writeOrders(delivered);
        } catch (SQLException dbUnavailable) {
            System.err.println("Something went wrong when trying to communicate with database. Please try again.");
            System.exit(1);
        }
    }
}