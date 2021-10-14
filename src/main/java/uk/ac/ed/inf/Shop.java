package uk.ac.ed.inf;

import java.util.List;

/**
 * Represents the shops included in the menu file from the web server,
 * containing their name, location, and the list of items on their menu.
 */
public class Shop {
    /** the name of the shop */
    private String name;
    /** the location of the shop*/
    private String location;
    /** a list of each item on the menu of this shop */
    private List<Item> menu;

    public List<Item> getMenu() {
        return menu;
    }

    /**
        * Represents the items on the menus of each shop from the json menu file,
        * containing their name and the price, in pence.
        */
        public static class Item{
            /** the name of the item */
            private String item;
            /** the price of the item, in pence */
            private int pence;

        /**
          * @return a String containing item name
         */
        public String getItem() {
            return item;
        }

        /**
         * @return an integer containing the price 
         */
        public int getPence() {
            return pence;
        }
    }
}
