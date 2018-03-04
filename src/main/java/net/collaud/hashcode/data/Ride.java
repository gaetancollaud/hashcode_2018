package net.collaud.hashcode.data;

import lombok.Data;
import lombok.experimental.Builder;
import net.collaud.hashcode.common.data.Point2DInt;

@Builder
@Data
public class Ride {
	private int id;
	private Point2DInt start;
	private Point2DInt end;
	private int earliestStart;
	private int latestFinish;
	private boolean done = false;
	private int sort;

	public int timeOfTravel() {
		return end.squareDistance(start);
	}

	public boolean canBeTaken(int currentTime) {
		return (currentTime + timeOfTravel()) <= latestFinish;
	}

	public int earlyStartToArriveInTime(Point2DInt carPos) {
		sort = this.earliestStart - carPos.squareDistance(start);
		return sort;
	}

	public int getTime(){
		return this.start.squareDistance(this.end);
	}
}
