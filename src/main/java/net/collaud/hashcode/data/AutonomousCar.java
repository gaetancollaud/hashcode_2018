package net.collaud.hashcode.data;

import lombok.Data;
import lombok.experimental.Builder;
import net.collaud.hashcode.common.data.Point2DInt;

import java.util.List;

@Builder
@Data
public class AutonomousCar {
	private int id;
	private Point2DInt currentPosition = new Point2DInt(0, 0);
	private Ride currentRide;
	private List<Ride> assignedRide;

	public void print(StringBuilder sb) {
		sb.append(assignedRide.size());
		assignedRide.forEach(r -> sb.append(' ').append(r.getId()));
		sb.append('\n');
	}
}
