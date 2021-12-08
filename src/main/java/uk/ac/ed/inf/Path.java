package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Contains the functions required for the application to generate the path the drone will
 * take and write the details of the path taken to the output file and tables.
 */
public class Path {
    /** the list of orders from the database */
    private final ArrayList<Order> orders;
    /** the lines which define the convex hull of the points defining the no-fly zone */
    private final ArrayList<Line2D.Double> convexHullLines = new ArrayList<>();
    /** a list of landmarks obtained from the server */
    private final ArrayList<LongLat> landmarks;
    /** each of the shops and the coordinates of their locations */
    private final HashMap<String, LongLat> shopLocations;
    /** the location of appleton tower */
    private final LongLat appletonTower;
    /** a list of the moves made by the drone */
    private final ArrayList<Move> moves = new ArrayList<>();
    /** the lines representing each move which is made by the drone */
    private final ArrayList<Point> movesLines = new ArrayList<>();
    /** the current location of the drone, starts at appleton tower */
    private LongLat currentLoc;
    /** the total cost of all the orders from the day */
    double totalCost = 0;
    /** the total cost of all the orders which are delivered */
    double deliveredCost = 0;
    /** a list of all the orders which are delivered */
    ArrayList<Order> delivered = new ArrayList<>();
    /** the total amount of orders */
    double orderCount;
    /** the current order being picked up or delivered */
    Order currentOrder;


    /**
     * Constructor for the Path class. The points which define the no-fly zone are obtained
     * from the server and used to create a convex hull of all the points in the no-fly zone.
     * The shop locations and Appleton Tower are added to the list of landmarks obtained from
     * the web server.
     *
     * @param orderList the list of orders obtained from the database
     * @param server    an instance of WebServer, used to get the building locations
     */
    public Path(ArrayList<Order> orderList, WebServer server){
        orders = orderList;
        orderCount = orders.size();
        final ArrayList<Point> noFlyZonePoints = server.parseNoFlyZone();
        appletonTower = new LongLat(-3.186874, 55.944494);
        currentLoc = appletonTower;
        landmarks = server.parseLandmarks();
        shopLocations = server.getLocationMap();
        landmarks.addAll(shopLocations.values());
        landmarks.add(appletonTower);
        convexHull(noFlyZonePoints);
    }

    /**
     * Creates a convex hull of the points defining the no-fly zone. It finds the leftmost
     * point and moves counter-clockwise round the points the find a selection of points
     * which surround the rest of the points, until it returns to the leftmost point.
     *
     * @param points the points which define the no-fly zone that create the convex hull
     */
    private void convexHull(List<Point> points) {
        List<Point> result = new ArrayList<>();
        int length = points.size();

        int leftmost = 0;
        for (int i = 1; i<length; i++)
            if (points.get(i).longitude()<points.get(leftmost).longitude())
                leftmost = i;

        int hullPoint = leftmost;
        int endpoint;

        do {
            result.add(points.get(hullPoint));
            endpoint = (hullPoint + 1) % length;
            for (int i = 0; i<length; i++) {
                if (Orientation(points.get(hullPoint), points.get(i), points.get(endpoint))) {
                    endpoint = i;
                }
            }
            hullPoint = endpoint;
        }
        while (hullPoint != leftmost);

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

    /**
     * Checks the orientation of 3 points, that value2 is counter-clockwise for these points.
     *
     * @param value1 the 1st value to get checked
     * @param value2 the 2nd value to get checked
     * @param value3 the 3rd value to get checked
     * @return true if value2 is most counter-clockwise
     */
    private static boolean Orientation(Point value1, Point value2, Point value3) {
        double check = (value2.latitude() - value1.latitude()) * (value3.longitude() - value2.longitude()) -
                (value2.longitude() - value1.longitude()) * (value3.latitude() - value2.latitude());
        return check < 0;
    }

    /**
     * Generates the path taken by the drone. It calculates the total cost for the orders made.
     * Until there are no more orders, or it runs out of moves, it then selects the order with the
     * closest shop to its current location, and moves towards this shop to collect the items.
     * Once it has arrived at each of the shops it checks again that it has enough moves to return to
     * Appleton Tower, and then takes the items to the delivery location. After delivery, it updates
     * the total cost of delivered items, and the list of delivered orders, before removing the order
     * from the order list.
     *
     * @return an ArrayList containing all the orders which were successfully delivered
     */
    public ArrayList<Order> generatePath() {
        for (Order order: orders) {
            totalCost += order.getCost();
        }
        boolean outOfMoves;
        while (!orders.isEmpty()) {
            currentOrder = chooseOrder();
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

    /**
     * Chooses the next order to be collected and delivered by the drone. It chooses whichever order
     * has a shop to collect items from which is closest to the drone's current location.
     *
     * @return an Order containing the chosen order
     */
    private Order chooseOrder() {
        double closestDist = Double.POSITIVE_INFINITY;
        double currentDist;
        Order currentOrd = orders.get(0);
        for (Order order : orders) {
            for (String shop: order.getShopList()) {
                currentDist = currentLoc.distanceTo(shopLocations.get(shop));
                if (currentDist < closestDist) {
                    closestDist = currentDist;
                    currentOrd = order;
                }
            }
        }
        return currentOrd;
    }

    /**
     * Carries out the functions to complete the drone delivery service. The drone returns to
     * Appleton Tower. It then outputs the delivered cost and the total cost, as well as the percentage
     * of the total cost that was successfully delivered, and the percentage of orders which were
     * successfully delivered. It also outputs the total number of moves made during the day's operations.
     */
    private void endDeliveries() {
        currentOrder = new Order(null);
        moveToGoal(appletonTower);
        System.out.println("delivered: " + deliveredCost);
        System.out.println("total: " + totalCost);
        System.out.println("percentage income: " + (deliveredCost/totalCost)*100 + "%");
        System.out.println("percentage deliveries: " + (delivered.size()/orderCount)*100 + "%");
        System.out.println("moves: " + moves.size());
    }

    /**
     * Collects items of the current order from the respective shops. For each item in the order,
     * it finds the shop closest to the current location and moves towards it until it is close to it.
     * It then hovers at the shop to collect the item, and updates the moves to reflect this. It
     * then checks if the drone needs to return to Appleton tower.
     *
     * @param currentOrder the current order which is being collected
     * @return a boolean stating whether the drone must finish deliveries
     */
    private boolean getOrder(Order currentOrder) {
        boolean noMoves = false;
        ArrayList<String> shops = new ArrayList<>(currentOrder.getShopList());
        while (!shops.isEmpty()) {
            String currentShop = closestShop(shops);
            LongLat goal = shopLocations.get(currentShop);
            findGoal(goal);
            currentLoc = currentLoc.nextPosition(-999);
            updateMoves(currentLoc,currentLoc,-999);
            noMoves = checkMoves();
            shops.remove(currentShop);
        }
        return noMoves;
    }

    /**
     * @param shopList the list of shops to be searched
     * @return a String containing the name of the closest shop
     */
    private String closestShop(ArrayList<String> shopList) {
        double minDist = Double.POSITIVE_INFINITY;
        String currentShop = "";
        for (String shop : shopList) {
            if (currentLoc.distanceTo(shopLocations.get(shop)) < minDist) {
                minDist = currentLoc.distanceTo(shopLocations.get(shop));
                currentShop = shop;
            }
        }
        return currentShop;
    }

    /**
     * Delivers the collected items to the specified delivery point. It moves towards
     * the delivery point until it is close to it, then hovers for a move to drop off the
     * order, and updates the moves to reflect this. It then checks if the drone needs to
     * return to Appleton tower.
     *
     * @param currentOrder the current order
     * @return a boolean stating whether the drone must finish deliveries
     */
    private boolean deliverOrder(Order currentOrder) {
        LongLat goal = currentOrder.getDeliverTo();
        findGoal(goal);
        currentLoc = currentLoc.nextPosition(-999);
        updateMoves(currentLoc,currentLoc,-999);
        return checkMoves();
    }

    /**
     * Checks whether the drone has enough moves to safely continue deliveries
     * after the last move made, or if it needs to end deliveries and return.
     *
     * @return a boolean stating whether the drone must finish deliveries
     */
    private boolean checkMoves() {
        return (1500 - moves.size()) <= 100;

    }

    /**
     * Finds the most advantageous goal for the drone to aim for. If the direct path
     * to the next goal passes over the no-fly zone, it instead moves towards the closest
     * landmark to the current goal, and then moves to the goal instead.
     *
     * @param goal a LongLat containing the current goal the drone needs to get to
     */
    private void findGoal(LongLat goal) {
        if (!validMove(currentLoc,goal)) {
            moveToGoal(closestLandmark(goal));
        }
        moveToGoal(goal);
    }

    /**
     * @param goalLoc the location of the current goal
     * @return a LongLat containing the coordinates of the landmark closest to the current goal
     */
    private LongLat closestLandmark(LongLat goalLoc) {
        double minDist = Double.POSITIVE_INFINITY;
        LongLat goal = goalLoc;
        for (LongLat landmark : landmarks) {
            if (goalLoc.distanceTo(landmark) < minDist & validMove(currentLoc,landmark)) {
                minDist = goalLoc.distanceTo(landmark);
                goal = landmark;
            }
        }
        return goal;
    }

    /**
     * While the drone is not close to the goal, it updates the current location
     * to the best move found to get to the goal.
     *
     * @param goal a LongLat containing the current goal
     */
    private void moveToGoal (LongLat goal) {
        while (!currentLoc.closeTo(goal)) {
            currentLoc = findMove(goal);
        }
    }

    /**
     * Finds the best move from the current location towards the current goal. It tests each possible
     * angle between 0 and 360, chooses the one which gets the closest to the goal, and updates the
     * moves to reflect the move being made.
     *
     * @param goal the current goal
     * @return a LongLat containing the new move
     */
    private LongLat findMove(LongLat goal) {
        int angle;
        LongLat newMove = currentLoc;
        LongLat testMove;
        int chosenAngle = 0;
        double minDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 36; i++) {
            angle = i*10;
            testMove = currentLoc.nextPosition(angle);
            if (validMove(currentLoc,testMove) & (testMove.distanceTo(goal) < minDist)) {
                minDist = testMove.distanceTo(goal);
                newMove = testMove;
                chosenAngle = angle;
            }
        }
        updateMoves(currentLoc,newMove,chosenAngle);
        return newMove;
    }

    /**
     * Checks whether a move is within in the confinement area, and does not cross the convex
     * hull created around the no-fly zone.
     *
     * @param currentLoc the current location of the drone
     * @param newLoc     the prospective move being checked
     * @return a boolean stating whether the move is valid or not
     */
    private boolean validMove(LongLat currentLoc, LongLat newLoc) {
        boolean valid = true;
        Line2D.Double plannedMove = new Line2D.Double(currentLoc.longitude,
                currentLoc.latitude,newLoc.longitude,newLoc.latitude);

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

    /**
     * @return a FeatureCollection containing the created path features of the drone's journey
     */
    public FeatureCollection getPathFeatures() {
        Feature feature;
        FeatureCollection pathFeatures;

        feature = Feature.fromGeometry(LineString.fromLngLats(movesLines));
        pathFeatures = FeatureCollection.fromFeature(feature);
        return pathFeatures;
    }

    /**
     * @return an ArrayList of Moves describing the flightpath of the drone
     */
    public ArrayList<Move> getFlightpath() {
        return moves;
    }

    /**
     * @param pathFeatures the features of the path taken
     * @param day          the day of deliveries
     * @param month        the month of deliveries
     * @param year         the year of deliveries
     */
    public void writeGeoJSON(FeatureCollection pathFeatures, String day, String month, String year) {
        try {
            File myObj = new File("C:\\Users\\sarah\\Documents\\2021FirstSemester\\ilp\\drone-" + day + "-" + month + "-" + year + ".geojson");
            Files.deleteIfExists(myObj.toPath());
            if (myObj.createNewFile()){
                FileWriter myWriter = new FileWriter("C:\\Users\\sarah\\Documents\\2021FirstSemester\\ilp\\drone-" + day + "-" + month + "-" + year + ".geojson");
                myWriter.write(pathFeatures.toJson());
                myWriter.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void updateMoves(LongLat firstLoc, LongLat secondLoc,int angle) {
        movesLines.add(Point.fromLngLat(firstLoc.longitude, firstLoc.latitude));
        movesLines.add(Point.fromLngLat(secondLoc.longitude, secondLoc.latitude));
        moves.add(new Move(currentOrder.orderNo,firstLoc,angle,secondLoc));
    }
}