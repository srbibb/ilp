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

public class WebServer {

    /** The HTTP client used to send requests to the server */
    private final HttpClient client = HttpClient.newHttpClient();
    /** The name of the machine the web server is running on */
    private final String machineName;
    /** The port which the web server is running on */
    private final String portName;
    private final HashMap<String, String> shopMap;
    private final HashMap<String, LongLat> locationMap;

    /**
     * Constructor for WebServer class.
     * @param port specifies the port where the web server is running
     */
    public WebServer(String port) {
        machineName = "localhost";
        portName = port;
        shopMap = parseShops();
        locationMap = parseShopLocations();
    }

    /**
     * Gets the menu from the web server. It connects to the
     * web server with the specified name and at the specified port, and
     * gets the json file containing the menu. It parses the file into a
     * list of the Shop class, and then converts the name of each item and
     * price to a HashMap, to find prices.
     * @return HashMap of the name of each menu item and its price
     */
    public HashMap<String, Integer> parseMenu() {
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
     * gets the json file containing the menu. It parses the file into a
     * list of the Shop class, and then converts each shop and its
     * location in to a HashMap, to find the relevant shop for each item.
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

    public List<List<Point>> parseNoFlyZone() {
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

            List<List<Point>> coordinateLists = new ArrayList<>();
            for (Polygon polygon : polygons) {
                coordinateLists.add(polygon.coordinates().get(0));
            }

            return coordinateLists;

        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong when trying to communicate with server. Please try again.");
            System.exit(1);
            return null;
        }
    }

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

    public HashMap<String, String> getShopMap() {
        return shopMap;
    }

    public HashMap<String, LongLat> getLocationMap() {
        return locationMap;
    }
}
