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
    private final ArrayList<String> items = new ArrayList<>();
    private final String orderNo;
    private LongLat deliverTo;
    private String deliveryAddress;
    private Set<String> shops;
    private int cost;

    /**
     * Instantiates a new Order.
     *
     * @param orderNumber the order number
     */
    public Order(String orderNumber) {
        orderNo = orderNumber;
    }

    /**
     * Add item.
     *
     * @param orderItem the order item
     */
    public void addItem(String orderItem) {
        items.add(orderItem);
    }

    /**
     * Sets deliver to.
     *
     * @param w3w the w 3 w
     */
    public void setDeliverTo(LongLat w3w) {
        deliverTo = w3w;
    }

    /**
     * Sets delivery address.
     *
     * @param address the address
     */
    public void setDeliveryAddress(String address) {
        deliveryAddress = address;
    }

    /**
     * Sets shops.
     *
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
     * Sets cost.
     *
     * @param costInPence the cost in pence
     */
    public void setCost(int costInPence) {
        cost = costInPence;
    }

    /**
     * Gets order no.
     *
     * @return the order no
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * Gets shop list.
     *
     * @return the shop list
     */
    public Set<String> getShopList() {
        return shops;
    }

    /**
     * Gets deliver to.
     *
     * @return the deliver to
     */
    public LongLat getDeliverTo() {
        return deliverTo;
    }

    /**
     * Gets delivery address.
     *
     * @return the delivery address
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public ArrayList<String> getItems() {
        return items;
    }

    /**
     * Gets cost.
     *
     * @return the cost
     */
    public int getCost() {
        return cost;
    }
}
