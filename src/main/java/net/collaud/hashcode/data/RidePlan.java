package net.collaud.hashcode.data;

import lombok.Builder;
import lombok.Data;
import net.collaud.hashcode.common.data.Point2DInt;

import java.util.*;

@Data
@Builder
public class RidePlan {
	@Builder.Default
	private List<Ride> rides = new ArrayList<>();
	@Builder.Default
	private boolean takeOnlyIfStartOnTime = false;
	private int maxSteps;
	private int bonus;

	public void addRide(Ride ride) {
		rides.add(ride);
	}

	public boolean canTakeRide(Ride ride) {
		boolean test = testRide(ride);
		if (test) {
			this.addRide(ride);
		}
		return test;
	}


	public boolean testRide(Ride ride) {
		if (rides.isEmpty()) {
			return true;
		} else {
			Ride last = rides.get(rides.size() - 1);
			int end = last.getEarliestStart() + last.getTime() + last.getEnd().squareDistance(ride.getStart()) + ride.getTime()+1;
			if (end > maxSteps) {
				// we won't have the time
				return false;
			}
		}

		Optional<Ride> optBefore = Optional.empty();
		Optional<Ride> optAfter = Optional.empty();
		Collections.sort(rides, Comparator.comparing(Ride::getEarliestStart));
		for (Ride r : rides) {
			if (r.getEarliestStart() < ride.getEarliestStart()) {
				optBefore = Optional.of(r);
			}
			if (ride.getEarliestStart() < r.getEarliestStart()) {
				optAfter = Optional.of(r);
				break;
			}
		}

		Optional<Ride> optRide = Optional.of(ride);

		boolean okForBefore = canChain(optBefore, optRide);
		boolean okForAfter = canChain(optRide, optAfter);

		return okForBefore && okForAfter;
	}

	public long getPoints() {
		long point = 0;
		long steps = 0;
		Point2DInt lastPost = new Point2DInt(0, 0);
		for (Ride ride : rides) {
			steps += lastPost.squareDistance(ride.getStart());

//			if (ride.getEarliestStart() >= steps) {
//				point += bonus;
//			}

			int time = ride.getTime();
			steps += time;
			point += time;
		}
//		if (steps > maxSteps) {
//			point = -1;
//		}
		return point;


	}

	protected static boolean canChain(Optional<Ride> optLeft, Optional<Ride> optRight) {
		if (optLeft.isPresent() && optRight.isPresent()) {
			Ride left = optLeft.get();
			Ride right = optRight.get();
			int dist = left.getEnd().squareDistance(right.getStart());
			if (left.getEarliestStart() + left.getTime() + dist <= right.getEarliestStart()) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}


}
