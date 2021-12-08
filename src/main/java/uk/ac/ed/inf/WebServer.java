package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Contains the functions required for the application to be able to access
 * the web server. It gets the menu items and shop locations from the server,
 * as well as the no-fly zone and landmarks.
 */
public class WebServer {

    /** the HTTP client used to send requests to the server */
    private final HttpClient client = HttpClient.newHttpClient();
    /** the name of the machine the web server is running on */
    private final String machineName;
    /** the port which the web server is running on */
    private final String portName;
    /** each item and its price */
    private final HashMap<String, Integer> itemMap;
    /** each item and the shop which sells it */
    private final HashMap<String, String> shopMap;
    /** each shop and its location */
    private final HashMap<String, LongLat> locationMap;

    /**
     * Constructor for WebServer class.
     *
     * @param port specifies the port where the web server is running
     */
    public WebServer(String port) {
        machineName = "localhost";
        portName = port;
        shopMap = parseShops();
        locationMap = parseShopLocations();
        itemMap = parseMenu();
    }

    /**
     * Gets the menu from the web server. It connects to the
     * web server with the specified name and at the specified port, and
     * gets the json file containing the menu. It parses the file into a
     * list of the Shop class, and then converts the name of each item and
     * price to a HashMap, to find prices.
     *
     * @return HashMap of the name of each menu item and its price
     */
    private HashMap<String, Integer> parseMenu() {
        String urlString = "http://" + machineName +":" + portName + "/menus/menus.json";
        String menusInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.exit(1);
                }
                menusInput = response.body();
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                ArrayList<Shop> shops = new Gson().fromJson(menusInput, listType);
                HashMap<String, Integer> menu = new HashMap<>();
                for (Shop shop: shops) {
                    for (Shop.Item item: shop.getMenu()) {
                        menu.put(item.getItem(), item.getPence());
                    }
                }
                return menu;

            } catch (IOException | InterruptedException e) {
                System.err.println("Something went wrong when trying to communicate with server. Please try again.");
                System.exit(1);
                return null;
            }
        }
    }

    /**
     * Gets shops from the web server. It connects to the
     * web server with the specified name and at the specified port, and
     * gets the json file containing the menu. It parses the file into a
     * list of the Shop class, and then converts each item and the shop it
     * is sold in to a HashMap, to find the relevant shop for each item.
     *
     * @return HashMap of the name of each menu item and the shop which sells it
     */
    private HashMap<String, String> parseShops() {
        String urlString = "http://" + machineName +":" + portName + "/menus/menus.json";
        String shopInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.exit(1);
                }
                shopInput = response.body();
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                ArrayList<Shop> shops = new Gson().fromJson(shopInput, listType);
                HashMap<String, String> shopList = new HashMap<>();
                for (Shop shop: shops) {
                    for (Shop.Item item: shop.getMenu()) {
                        shopList.put(item.getItem(), shop.getShop());
                    }
                }
                return shopList;

            } catch (IOException | InterruptedException e) {
                System.err.println("Something went wrong when trying to communicate with server. Please try again.");
                System.exit(1);
                return null;
            }
        }
    }

    /**
     * Gets the locations of the shops from the web server. It connects to the
     * web server with the specified name and at the specified port, and
     * gets the json file containing the menu. It parses the file into a list of
     * the Shop class, and then converts each shop and the coordinates of the location
     * in to a HashMap, obtained from the server using the whatthreewords address,
     * to find the relevant location for each shop. The coordinates are obtained by
     * parsing the whatthreewords address.
     *
     * @return HashMap of the name of each shop and its location in what three words
     */
    private HashMap<String, LongLat> parseShopLocations() {
        String urlString = "http://" + machineName +":" + portName + "/menus/menus.json";
        String shopInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.exit(1);
                }
                shopInput = response.body();
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                ArrayList<Shop> shops = new Gson().fromJson(shopInput, listType);
                HashMap<String, LongLat> shopW3W = new HashMap<>();
                for (Shop shop: shops) {
                    shopW3W.put(shop.getShop(), parseWhatThreeWords(shop.getLocation()));
                }
                return shopW3W;

            } catch (IOException | InterruptedException e) {
                System.err.println("Something went wrong when trying to communicate with server. Please try again.");
                System.exit(1);
                return null;
            }
        }
    }

    /**
     * Gets the location of a whatthreewords address from the web server.
     * It connects to the web server with the specified name and at the specified
     * port, and gets the json file at the address defined by the whatthreewords
     * address. It parses the file into the What3Words class to get the coordinates.
     *
     * @param whatthreewords the whatthreewords address
     * @return LongLat containing the coordinates for this location
     */
    public LongLat parseWhatThreeWords(String whatthreewords) {
        /*
        turning the w3w into a longlat
        */
        String[] w3wParts = whatthreewords.split("\\.");
        String urlString = "http://" + machineName +":" + portName + "/words/" + w3wParts[0] + "/" +
                w3wParts[1] + "/" + w3wParts[2] + "/details.json";
        String w3wInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.exit(1);
            }
            w3wInput = response.body();
            Type listType = new TypeToken<What3Words>() {}.getType();
            What3Words what3words = new Gson().fromJson(w3wInput, listType);
            return new LongLat(what3words.getCoordinates());

        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong when trying to communicate with server. Please try again.");
            System.exit(1);
            return null;
        }

    }

    /**
     * Gets the location of the no-fly zone from the web server. It connects to the web
     * server with the specified name at the specified port, and gets the json file
     * containing the GeoJSON features of the no-fly zone. It converts each feature into
     * polygons, and then creates a list of all of the points which define the no-fly zone.
     *
     * @return a List of Lists of points outlining the no-fly zone
     */
    public ArrayList<Point> parseNoFlyZone() {
        String urlString = "http://" + machineName +":" + portName + "/buildings/no-fly-zones.geojson";
        String mapInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.exit(1);
            }
            mapInput = response.body();
            List<Feature> features = FeatureCollection.fromJson(mapInput).features();

            ArrayList<Polygon> polygons = new ArrayList<>();

            for (Feature feature: features) {
                polygons.add((Polygon)feature.geometry());
            }

            ArrayList<Point> coordinates = new ArrayList<>();
            for (Polygon polygon : polygons) {
                coordinates.addAll(polygon.coordinates().get(0));
            }

            return coordinates;

        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong when trying to communicate with server. Please try again.");
            System.exit(1);
            return null;
        }
    }

    /**
     * Gets the location of the landmarks from the web server. It connects to the web
     * server with the specified name at the specified port, and gets the json file
     * containing the GeoJSON features of the no-fly zone. It then creates a new LongLat
     * from the points contained in each feature.
     *
     * @return a List of LongLats representing the locations of the landmarks
     */
    public ArrayList<LongLat> parseLandmarks() {
        String urlString = "http://" + machineName +":" + portName + "/buildings/landmarks.geojson";
        String mapInput;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        final ArrayList<LongLat> landmarks = new ArrayList<>();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.exit(1);
            }
            mapInput = response.body();

            List<Feature> features = FeatureCollection.fromJson(mapInput).features();

            for (Feature feature: features) {
                Point point = ((Point)feature.geometry());
                landmarks.add(new LongLat(point.longitude(), point.latitude()));
            }

            return landmarks;

        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong when trying to communicate with server. Please try again.");
            System.exit(1);
            return null;
        }
    }

    /**
     * @return a HashMap containing each item and its shop
     */
    public HashMap<String, String> getShopMap() {
        return shopMap;
    }

    /**
     * @return a HashMap containing each shop and its location
     */
    public HashMap<String, LongLat> getLocationMap() {
        return locationMap;
    }

    /**
     * @return a HashMap containing each item and its price
     */
    public HashMap<String, Integer> getItemMap() {
        return itemMap;
    }
}
