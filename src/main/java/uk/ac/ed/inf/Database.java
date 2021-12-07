package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains the functions required for the application to be able to access
 * the database. It gets the orders made by users and writes the orders delivered
 * and the flightpath taken after the given day of deliveries is completed.
 */
public class Database {
    /**
     * The day to find orders for
     */
    private final String day;
    /**
     * The month to find orders for
     */
    private final String month;
    /**
     * The year to find orders for
     */
    private final String year;
    /**
     * The string constructed to access the database containing the machine name, port name, and database name
     */
    private final String jdbcString;

    /**
     * Constructor for Database class.
     *
     * @param inputDay   specifies the day to find orders for
     * @param inputMonth specifies the month to find orders for
     * @param inputYear  specifies the year to find orders for
     * @param port       specifies the port where the database is running
     */
    public Database(String inputDay, String inputMonth, String inputYear, String port) {
        String machineName = "localhost";
        day = inputDay;
        month = inputMonth;
        year = inputYear;
        jdbcString = "jdbc:derby://" + machineName + ":" + port + "/derbyDB";
    }

    /**
     * Gets the orders for the requested day, which are stored on the database.
     * It connects to the database and creates an sql query to specify the needed
     * orders. It then finds the items associated with each order, and also assigns the
     * delivery address, each shop to be visited, and coordinates to the instance of Order.
     *
     * @param server the server, used to find the locations of each shop
     * @return an ArrayList containing the orders received from the database
     * @throws SQLException the sql exception if the database is not available
     */
    public ArrayList<Order> getOrders(WebServer server) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        Connection conn = DriverManager.getConnection(jdbcString);
        String date = year + "-" + month + "-" + day;
        Date orderDate = java.sql.Date.valueOf(date);
        ArrayList<String> orderList = new ArrayList<>();
        final String orderQuery =
                "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrderQuery =
                conn.prepareStatement(orderQuery);
        psOrderQuery.setDate(1, orderDate);
        ResultSet rs = psOrderQuery.executeQuery();
        while (rs.next()) {
            String order = rs.getString("orderNo");
            orderList.add(order);
        }

        int i = 0;
        for (String order : orderList) {
            orders.add(new Order(order));
            final String orderDetailsQuery =
                    "select * from orderDetails where orderNo=(?)";
            PreparedStatement psOrderDetailsQuery =
                    conn.prepareStatement(orderDetailsQuery);
            psOrderDetailsQuery.setString(1, order);
            ResultSet rsDetails = psOrderDetailsQuery.executeQuery();
            while (rsDetails.next()) {
                String orderDetail = rsDetails.getString("item");
                orders.get(i).addItem(orderDetail);
            }
            final String orderW3WQuery =
                    "select * from orders where orderNo=(?)";
            PreparedStatement psOrderW3WQuery =
                    conn.prepareStatement(orderW3WQuery);
            psOrderW3WQuery.setString(1, order);
            ResultSet rsW3W = psOrderW3WQuery.executeQuery();
            while (rsW3W.next()) {
                String orderW3W = rsW3W.getString("deliverTo");
                orders.get(i).setDeliveryAddress(orderW3W);
                orders.get(i).setDeliverTo(server.parseWhatThreeWords(orderW3W));
            }
            HashMap<String, String> shopMap = server.getShopMap();
            orders.get(i).setShops(shopMap);
            i += 1;
        }
        return orders;
    }

    /**
     * Writes the orders which were successfully delivered during the day's deliveries
     * to the database. It will check if a deliveries table already exists and drop
     * it if it does, and then write each order with its order number, delivery address and
     * the cost of the order in pence to the newly created deliveries table.
     *
     * @param orders the orders which were delivered by the drone
     * @throws SQLException the sql exception if the database is not available
     */
    public void writeOrders(ArrayList<Order> orders) throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();

        DatabaseMetaData databaseMetadata = conn.getMetaData();

        ResultSet resultSet =
                databaseMetadata.getTables(null, null, "DELIVERIES", null);
        if (resultSet.next()) {
            statement.execute("drop table deliveries");
        }

        statement.execute(
                "create table deliveries(" +
                        "orderNo char(8)," +
                        "deliveredTo varchar(19)," +
                        "costInPence int)");

        final String deliveriesStatement = "insert into deliveries values (?, ?, ?)";
        PreparedStatement psDeliveries = conn.prepareStatement(deliveriesStatement);

        for (Order order : orders) {
            psDeliveries.setString(1, order.getOrderNo());
            psDeliveries.setString(2, order.getDeliveryAddress());
            psDeliveries.setInt(3, order.getCost());
            psDeliveries.execute();
        }

    }

    /**
     * Writes the flightpath the drone took while delivering the day's orders
     * to the database. It will check if a deliveries table already exists and drop
     * it if it does, and then write each move the drone made. This includes the starting
     * latitude and longitude, the ending latitude and longitude, the angle the move was
     * made at, and order number the drone was picking up or delivering when the move was
     * made. If the drone is returning to Appleton Tower at the end of the day, the order
     * number value will contain null instead.
     * @param moves the moves
     * @throws SQLException the sql exception
     */
    public void writeFlightpath(ArrayList<Move> moves) throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();

        DatabaseMetaData databaseMetadata = conn.getMetaData();

        ResultSet resultSet =
                databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
        if (resultSet.next()) {
            statement.execute("drop table flightpath");
        }


        statement.execute(
                "create table flightpath(" +
                        "orderNo char(8)," +
                        "fromLongitude double," +
                        "fromLatitude double," +
                        "angle int," +
                        "toLongitude double," +
                        "toLatitude double)");


        final String flightpathStatement = "insert into flightpath values (?, ?, ?, ?, ?, ?)";
        PreparedStatement psFlightpath = conn.prepareStatement(flightpathStatement);

        for (Move move: moves) {
            psFlightpath.setString(1, move.getOrderNo());
            psFlightpath.setDouble(2, move.getStartPoint().getLongitude());
            psFlightpath.setDouble(3, move.getStartPoint().getLatitude());
            psFlightpath.setInt(4,move.getAngle());
            psFlightpath.setDouble(5,move.getEndPoint().getLongitude());
            psFlightpath.setDouble(6,move.getEndPoint().getLatitude());
            psFlightpath.execute();
        }


    }
}
