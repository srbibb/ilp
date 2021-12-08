package uk.ac.ed.inf;

/**
 * Represents a move made by the drone during delivery. Contains the order number of the
 * current delivery, the beginning of the move in both latitude and longitude, the
 * angle the move was made at, and the end of the move in both latitude and longitude.
 */
public class Move {
    /** the orderNo of the order being delivered at the time of the move, contains null
     * if returning at the end of deliveries */
    public final String orderNo;
    /**
     * the point where the drone started the move
     */
    public final LongLat startPoint;
    /**
     * the angle at which the move was made
     */
    public final int angle;
    /**
     * the point where the drone ended the move
     */
    public final LongLat endPoint;

    /**
     * Constructor for the Move class.
     *
     * @param number      the order number of the current order
     * @param start       the start point of this move
     * @param angleOfMove the angle of the move
     * @param end         the end point of this move
     */
    public Move(String number, LongLat start, int angleOfMove,
                LongLat end) {
        orderNo = number;
        startPoint = start;
        angle = angleOfMove;
        endPoint = end;
    }
}
