package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Path {
    ArrayList<Order> orders;
    LongLat currentLoc;
    private final ArrayList<Line2D.Double> noFlyZoneLines = new ArrayList<>();
    private final LongLat appletonTower;
    private final ArrayList<LineString> movesLines = new ArrayList<>();
    private final ArrayList<LongLat> moves = new ArrayList<>();
    private final ArrayList<LongLat> landmarks;
    private final HashMap<String, LongLat> shopLocations;
    List<List<Point>> noFlyZonePoints;
    ArrayList<Point> allPoints = new ArrayList<>();
    ArrayList<Line2D.Double> convexHullLines = new ArrayList<>();
    double totalCost = 0;
    double deliveredCost = 0;
    ArrayList<LongLat> currentPath = new ArrayList<>();
    ArrayList<Order> delivered = new ArrayList<>();


    public Path(ArrayList<Order> orderList, WebServer server){
        orders = orderList;
        /** The WebServer which can parse information on menu items and cost */
        noFlyZonePoints = server.parseNoFlyZone();
        appletonTower = new LongLat(-3.186874, 55.944494);
        currentLoc = appletonTower;
        landmarks = server.parseLandmarks();
        shopLocations = server.parseShopLocations();

        for (List<Point> building : noFlyZonePoints) {
            for (int i=0; i < building.size()-1; i++) {
                Line2D.Double line = new Line2D.Double(building.get(i).longitude(),building.get(i).latitude(),
                        building.get(i+1).longitude(),building.get(i+1).latitude());
                noFlyZoneLines.add(line);
            }
        }

        for (List<Point> building : noFlyZonePoints) {
            allPoints.addAll(building);
        }

        landmarks.addAll(shopLocations.values());
        convexHull(allPoints);
    }

    public void convexHull(List<Point> points) {
        List<Point> result = new ArrayList<>();
        int length = points.size();

        int leftmost = 0;
        for (int i = 1; i<length; i++)
            if (points.get(i).longitude()<points.get(leftmost).longitude())
                leftmost = i;

        int p = leftmost, pointq;

        do {
            result.add(points.get(p));
            pointq = (p + 1) % length;
            for (int i = 0; i<length; i++) {
                if (OrientationMatch(points.get(p), points.get(i), points.get(pointq)) == 2) {
                    pointq = i;
                }
            }
            p = pointq;
        }
        //TODO why is this like this (make whole function understandable)
        while (p != leftmost);
        int resultSize = result.size();
        for (int i = 0; i < resultSize-1; i++) {
            Line2D.Double line = new Line2D.Double(result.get(i).longitude(),result.get(i).latitude(),
                    result.get(i+1).longitude(),result.get(i+1).latitude());
            convexHullLines.add(line);
        }
        Line2D.Double line = new Line2D.Double(result.get(resultSize-1).longitude(),result.get(resultSize-1).latitude(),
                result.get(0).longitude(),result.get(0).latitude());
        convexHullLines.add(line);
    }

    public static int OrientationMatch(Point check1, Point check2, Point check3) {
        double val = (check2.latitude() - check1.latitude()) * (check3.longitude() - check2.longitude()) -
                (check2.longitude() - check1.longitude()) * (check3.latitude() - check2.latitude());
        if (val == 0)
            return 0;
        return (val > 0) ? 1 : 2;
    }

    public LongLat getShopLocation(String shop) {
        return shopLocations.get(shop);
    }

    public ArrayList<Order> generatePath(Menus menu) {
        for (Order order: orders) {
            order.setCost(getOrderPrice(menu,order));
            totalCost += order.getCost();
        }
        boolean outOfMoves;
        while (!orders.isEmpty()) {
            Order currentOrder = chooseOrder();
            outOfMoves = getOrder(currentOrder);
            if (outOfMoves) {
                break;
            }
            outOfMoves = deliverOrder(currentOrder);
            deliveredCost += currentOrder.getCost();
            delivered.add(currentOrder);
            orders.remove(currentOrder);
            if (outOfMoves) {
                break;
            }
        }
        endDeliveries();
        return delivered;
    }

    public Order chooseOrder() {
        //money
        double highestCost = 0;
        double currentCost;
        Order currentOrd = orders.get(0);
        for (Order order : orders) {
            currentCost = order.getCost();
            if (currentCost > highestCost) {
                highestCost = currentCost;
                currentOrd = order;

            }
        }
        System.out.println("order chosen");
        return currentOrd;
    }

    public Order chooseOrderDist() {
        //distance
        double closestDist = Double.POSITIVE_INFINITY;
        double currentDist;
        Order currentOrd = orders.get(0);
        for (Order order : orders) {
            for (String shop: order.getShopList()) {
                currentDist = currentLoc.distanceTo(getShopLocation(shop));
                if (currentDist < closestDist) {
                    closestDist = currentDist;
                    currentOrd = order;
                }
            }
        }
        System.out.println("order chosen");
        return currentOrd;
    }

    public int getOrderPrice(Menus menus, Order currentOrder) {
        return menus.getDeliveryCost(currentOrder.getItems());
    }

    public void endDeliveries() {
        moveToGoal(appletonTower);
        System.out.println("delivered: " + deliveredCost);
        System.out.println("total: " + totalCost);
        System.out.println("percentage: " + (deliveredCost/totalCost)*100 + "%");
        System.out.println("moves: " + moves.size());
        getPathFeatures();
    }

    public boolean getOrder(Order currentOrder) {
        boolean noMoves = false;
        for (String shop : currentOrder.getShopList()) {
            LongLat goal = getShopLocation(shop);
            findGoal(goal);
            currentLoc = currentLoc.nextPosition(-999);
            System.out.println("reached: " + shop);
            noMoves = checkMoves();
        }
        return noMoves;
    }

    public boolean deliverOrder(Order currentOrder) {
        LongLat goal = currentOrder.getDeliverTo();
        findGoal(goal);
        currentLoc = currentLoc.nextPosition(-999);
        System.out.println("delivered order!");
        return checkMoves();
    }

    public boolean checkMoves() {
        return (1500 - moves.size()) < 100;
        /*
        LongLat tempCurrentLoc = currentLoc;
        findGoal(appletonTower);
        int distance = currentPath.size();
        currentLoc = tempCurrentLoc;
        boolean noMoves = ((1500-moves.size())<distance+50);
        return noMoves;

         */

    }

    public void findGoal(LongLat goal) {
        if (!validMove(currentLoc,goal)) {
            moveToGoal(closestLandmark(goal));
        }
        moveToGoal(goal);
    }

    public LongLat closestLandmark(LongLat shopLoc) {
        double minDist = Double.POSITIVE_INFINITY;
        LongLat goal = shopLoc;
        for (LongLat landmark : landmarks) {
            if (shopLoc.distanceTo(landmark) < minDist & validMove(currentLoc,landmark)) {
                minDist = shopLoc.distanceTo(landmark);
                goal = landmark;
            }
        }
        return goal;
    }

    public void moveToGoal (LongLat goal) {
        while (!currentLoc.closeTo(goal)) {
            currentLoc = findMove(goal);
        }
    }

    public LongLat findMove(LongLat goal) {
        int angle;
        LongLat newMove = currentLoc;
        LongLat testMove;
        double minDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 36; i++) {
            angle = i*10;
            testMove = currentLoc.nextPosition(angle);
            if (validMove(currentLoc,testMove) & (testMove.distanceTo(goal) < minDist)) {
                minDist = testMove.distanceTo(goal);
                newMove = testMove;
            }
        }

        Point currentPoint = Point.fromLngLat(currentLoc.longitude, currentLoc.latitude);
        Point newPoint = Point.fromLngLat(newMove.longitude, newMove.latitude);
        List<Point> points = Arrays.asList(currentPoint,newPoint);
        movesLines.add(LineString.fromLngLats(points));
        moves.add(newMove);
        return newMove;
    }

    public boolean validMove(LongLat currentLoc, LongLat newLoc) {
        boolean valid = true;
        Line2D.Double plannedMove = new Line2D.Double(currentLoc.longitude,currentLoc.latitude,newLoc.longitude,newLoc.latitude);

        for (Line2D.Double line : noFlyZoneLines) {
            if (plannedMove.intersectsLine(line)) {
                valid = false;
            }
        }

        for (Line2D.Double line : convexHullLines) {
            if (plannedMove.intersectsLine(line)) {
                valid = false;
            }
        }

        if (!newLoc.isConfined()) {
            valid = false;
        }
        return valid;
    }

    public FeatureCollection getPathFeatures() {
        ArrayList<Feature> feature = new ArrayList<>();
        FeatureCollection pathFeatures;

        for (List<Point> building : noFlyZonePoints) {
            feature.add(Feature.fromGeometry(LineString.fromLngLats(building)));
        }

        for (LineString line : movesLines) {
            feature.add(Feature.fromGeometry(line));
        }

        for (LongLat landmark : landmarks) {
            feature.add(Feature.fromGeometry(Point.fromLngLat(landmark.longitude, landmark.latitude)));
        }

        pathFeatures = FeatureCollection.fromFeatures(feature);
        return pathFeatures;
    }
}
