package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import  java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Menus {
    String machineName;
    String portName;
    List<Shop> shops;
    private static final HttpClient client = HttpClient.newHttpClient();

    public Menus(String name, String port) {
        machineName = name;
        portName = port;
    }

    public void parse() {
        String urlString = "http://" + machineName +":" + portName + "/menus/menus.json";
        String menus;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        HttpResponse<String> response;
        {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.exit(1);
                }
                menus = response.body();
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                ArrayList<Shop> shopList = new Gson().fromJson(menus, listType);
                shops = shopList;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public int getDeliveryCost(String...deliveries) {
        parse();
        int price = 50;
        for (String wantedItem : deliveries) {
            for (Shop shop: shops) {
                for (Shop.Item item: shop.menu) {
                    if (item.item.equals(wantedItem)) {
                        price += item.pence;
                    }
                }
            }
        }
        return price;
    }


}
