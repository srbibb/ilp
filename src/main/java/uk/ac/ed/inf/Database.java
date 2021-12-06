package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

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
     * @param inputDay specifies the day to find orders for
     * @param inputMonth specifies the month to find orders for
     * @param inputYear specifies the year to find orders for
     * @param port specifies the port where the database is running
     */
    public Database(String inputDay, String inputMonth, String inputYear, String port) {
        /**
         * The name of the machine the database is running on
         */
        String machineName = "localhost";
        /**
         * The port which the database is running on
         */
        day = inputDay;
        month = inputMonth;
        year = inputYear;
        jdbcString = "jdbc:derby://" + machineName + ":" + port + "/derbyDB";
    }

    public ArrayList<Order> getOrders(WebServer server) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        Connection conn = DriverManager.getConnection(jdbcString);
        String orderDate = year + "-" + month + "-" + day;
        ArrayList<String> orderList = new ArrayList<>();
        final String orderQuery =
                "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrderQuery =
                conn.prepareStatement(orderQuery);
        psOrderQuery.setString(1, orderDate);
        ResultSet rs = psOrderQuery.executeQuery();
        while (rs.next()) {
            String order = rs.getString("orderNo");
            orderList.add(order);
        }

        int orderNo = 0;
        for (String order : orderList) {
            orders.add(new Order());
            orders.get(orderNo).setOrderNo(order);
            final String orderDetailsQuery =
                    "select * from orderDetails where orderNo=(?)";
            PreparedStatement psOrderDetailsQuery =
                    conn.prepareStatement(orderDetailsQuery);
            psOrderDetailsQuery.setString(1, order);
            ResultSet rsDetails = psOrderDetailsQuery.executeQuery();
            while (rsDetails.next()) {
                String orderDetail = rsDetails.getString("item");
                orders.get(orderNo).addItem(orderDetail);
            }
            final String orderW3WQuery =
                    "select * from orders where orderNo=(?)";
            PreparedStatement psOrderW3WQuery =
                    conn.prepareStatement(orderW3WQuery);
            psOrderW3WQuery.setString(1, order);
            ResultSet rsW3W = psOrderW3WQuery.executeQuery();
            while (rsW3W.next()) {
                String orderW3W = rsW3W.getString("deliverTo");
                orders.get(orderNo).setDeliveryAddress(orderW3W);
                orders.get(orderNo).setDeliverTo(server.parseWhatThreeWords(orderW3W));
            }
            HashMap<String, String> shopMap = server.getShopMap();
            orders.get(orderNo).setShops(shopMap);
            orderNo += 1;
        }
        return orders;
    }

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
