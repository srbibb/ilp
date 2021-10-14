package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import  java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Contains the functions required for the drone
 * to use the menu system contained in the web server.
 */
public class Menus {

    /** The name of the machine the web server is running on */
    private final String machineName;
    /** The port which the web serve is running on */
    private final String portName;
    /** The list of shops contained in the file on the web server */
    ArrayList<Shop> shops;
    /** The HTTP client used to send requests to the server */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Constructor for Menus class.
     * @param name specifies the name of the machine where the web server is running
     * @param port specifies the port where the web server is running
     */
    public Menus(String name, String port) {

        machineName = name;
        portName = port;
    }

    /**
     * Gets the menu from the web server. It connects to the
     * web server with the specified name and at the specified port, and
     * gets the json file containing the menu. It parses the file into a
     * list of the Shop class, to be used to find prices.
     */
    public void parse() {
        String urlString = "http://" + machineName +":" + portName + "/menus/menus.json";
        String menus;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        {
            try {
                response = client.send(request, BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.exit(1);
                }
                menus = response.body();
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                shops = new Gson().fromJson(menus, listType);

            } catch (IOException | InterruptedException e) {
                System.err.println("Something went wrong when trying to communicate with server. Please try again.");
                System.exit(1);
            }
        }
    }

    /**
     * Takes an order as a list of items to be delivered and calculates
     * the price. It finds the prices from the lists of menus for each shop, and
     * combines these prices with the service cost of 50p to return the final price.
     * @param deliveries a list of Strings containing an order of items from the menu
     * @return the calculated price in pence
     */
    public int getDeliveryCost(String...deliveries) {
        parse();
        int price = 50;
        for (String wantedItem : deliveries) {
            for (Shop shop: shops) {
                for (Shop.Item item: shop.getMenu()) {
                    if (item.getItem().equals(wantedItem)) {
                        price += item.getPence();
                    }
                }
            }
        }
        return price;
    }


}
