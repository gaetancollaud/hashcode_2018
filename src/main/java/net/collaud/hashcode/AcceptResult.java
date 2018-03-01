package net.collaud.hashcode;

import lombok.Data;
import net.collaud.hashcode.common.data.Point2DInt;

/**
 * @author jgi
 */
@Data
public class AcceptResult {
    private RideInfo rideInfo;
    boolean startPerfect;
    boolean endPerfect;
    int index;
    int start;
    int end;
    int timeLost;
    int ratio;
    AutonomousCarInfo car;
    Point2DInt sourcePoint;
    Point2DInt targetPoint;

    public AutonomousCarInfo apply() {
        car.addRide(this, rideInfo);
        return car;
    }
}
