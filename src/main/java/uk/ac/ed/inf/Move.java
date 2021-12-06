package uk.ac.ed.inf;

public class Move {

    private final String orderNo;
    private final LongLat startPoint;
    private final int angle;
    private final LongLat endPoint;

    public Move(String number, LongLat start, int angleOfMove,
                LongLat end) {
        orderNo = number;
        startPoint = start;
        angle = angleOfMove;
        endPoint = end;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public LongLat getStartPoint() {
        return startPoint;
    }

    public int getAngle() {
        return angle;
    }

    public LongLat getEndPoint() {
        return endPoint;
    }
}
