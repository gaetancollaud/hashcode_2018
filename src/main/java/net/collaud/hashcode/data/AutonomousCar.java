package net.collaud.hashcode.data;

import lombok.Data;
import lombok.experimental.Builder;
import net.collaud.hashcode.AcceptResult;
import net.collaud.hashcode.common.data.Point2DInt;

import java.util.List;
import java.util.TreeSet;

@Builder
@Data
public class AutonomousCar {
	private int id;
	private Point2DInt currentPosition;
	private Ride currentRide;
	private List<Ride> assignedRide;

	private int nextStepAvailable;

	public void print(StringBuilder sb) {
		sb.append(assignedRide.size());
		assignedRide.forEach(r -> sb.append(' ').append(r.getId()));
		sb.append('\n');
	}

	public void assignRide(Ride ride) {
		ride.setDone(true);
		int timeToStartRide = ride.getStart().squareDistance(currentPosition);
		setCurrentPosition(ride.getEnd());
		nextStepAvailable = timeToStartRide + ride.timeOfTravel();
		assignedRide.add(ride);
	}
}
