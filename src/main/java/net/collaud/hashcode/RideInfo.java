package net.collaud.hashcode;

import lombok.Data;
import net.collaud.hashcode.data.Ride; /**
 * @author jgi
 */
@Data
public class RideInfo {
    int ratio;
    int timeSpent;
    Ride ride;
    int distance;
    int start;
    int end;

    public RideInfo(Ride ride, int ratio10) {
        this.ride = ride;
        this.distance = ride.getStart().squareDistance(ride.getEnd());
        this.timeSpent = ride.getLatestFinish() - ride.getEarliestStart();
        this.start = ride.getEarliestStart();
        this.end = ride.getLatestFinish();
        this.ratio = distance - ((timeSpent - distance) / ratio10);
    }
}
