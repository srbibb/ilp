package uk.ac.ed.inf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an order made by a user. Contains a list of the items ordered, the order number,
 * both the coordinates and the whatthreewords description of the place the order is delivered to,
 * the shops the drone needs to visit to collect the items and the cost of the order.
 */
public class Order {
    /** each of the items in the order */
    public final ArrayList<String> items = new ArrayList<>();
    /** the order number associated with the order */
    public final String orderNo;
    /** the coordinates the order should be delivered to */
    private LongLat deliverTo;
    /** the whatthreewords associated with the location the order should
     * be delivered to */
    private String deliveryAddress;
    /** the shop(s) the items in the order can be collected from */
    private Set<String> shops;
    /** the total cost of the order in pence */
    private int cost;

    /**
     * Constructor for the Order class.
     *
     * @param orderNumber the order number for the order
     */
    public Order(String orderNumber) {
        orderNo = orderNumber;
    }

    /**
     * @param w3w a LongLat containing the coordinates of the delivery address
     *            from the whatthreewords location
     */
    public void setDeliverTo(LongLat w3w) {
        deliverTo = w3w;
    }

    /**
     * @param address a String containing the whatthreewords address of
     *                the delivery location
     */
    public void setDeliveryAddress(String address) {
        deliveryAddress = address;
    }

    /**
     * @param shopMap the shop map
     */
    public void setShops(HashMap<String,String> shopMap) {
        ArrayList<String> shopList = new ArrayList<>();
        for (String item : items) {
            shopList.add(shopMap.get(item));
        }
        shops = new HashSet<>(shopList);
    }

    /**
     * @param itemMap a HashMap containing each item from the menu and its price
     */
    public void setCost(HashMap<String,Integer> itemMap) {
        int price = 50;
        for (String wantedItem : items) {
            price += itemMap.get(wantedItem);
        }
        cost = price;
    }

    /**
     * @return a Set containing each shop on the list without duplicates
     */
    public Set<String> getShopList() {
        return shops;
    }

    /**
     * @return a LongLat containing the coordinates to deliver to
     */
    public LongLat getDeliverTo() {
        return deliverTo;
    }

    /**
     * @return a String containing the whatthreewords delivery address
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * @return an integer containing the cost of the order
     */
    public int getCost() {
        return cost;
    }
}
