package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains the functions required for the drone
 * to use the menu system contained in the web server.
 */
public class Menus {
    /** The WebServer which can parse information on menu items and cost */
    private final WebServer server;
    /** A HashMap containing every item on each menu of each shop and their price */
    HashMap<String, Integer> menu;

    /**
     * Constructor for Menus class. Connects to the web server to assign prices
     * for each item from each menu to the menu HashMap.
     * @param WebServer WebServer which can parse information from the server
     */
    public Menus(WebServer WebServer) {
        server = WebServer;
        menu = server.parseMenu();
    }

    /**
     * Takes an order as a list of items to be delivered and calculates
     * the price. It finds the prices from the HashMap of prices for each item, and
     * combines these prices with the service cost of 50p to return the final price.
     * @param deliveries a list of Strings containing an order of items from the menu
     * @return the calculated price in pence
     */
    public int getDeliveryCost(ArrayList<String> deliveries) {
        int price = 50;
        for (String wantedItem : deliveries) {
            price += menu.get(wantedItem);
        }
        return price;
    }

}
