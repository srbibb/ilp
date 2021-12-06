package uk.ac.ed.inf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Order {
    private final ArrayList<String> items = new ArrayList<>();
    private String orderNo;
    private LongLat deliverTo;
    private String deliveryAddress;
    private Set<String> shops;
    private int cost;

    public Order(String orderNumber) {
        orderNo = orderNumber;
    }

    public void addItem(String orderItem) {
        items.add(orderItem);
    }

    public void setDeliverTo(LongLat w3w) {
        deliverTo = w3w;
    }

    public void setDeliveryAddress(String address) {
        deliveryAddress = address;
    }

    public void setShops(HashMap<String,String> shopMap) {
        ArrayList<String> shopList = new ArrayList<>();
        for (String item : items) {
            shopList.add(shopMap.get(item));
        }
        shops = new HashSet<>(shopList);
    }

    public void setCost(int costInPence) {
        cost = costInPence;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Set<String> getShopList() {
        return shops;
    }

    public LongLat getDeliverTo() {
        return deliverTo;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public int getCost() {
        return cost;
    }

    public void print() {
        for (String item : items) {
            System.out.println(item);
        }
        System.out.println(deliverTo);
        System.out.println(getShopList());
    }
}
