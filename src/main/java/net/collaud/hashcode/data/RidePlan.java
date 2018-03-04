package net.collaud.hashcode.data;

import lombok.*;

import java.util.*;

@Data
@Builder
public class RidePlan {
	@Builder.Default
	private List<Ride> rides = new ArrayList<>();
	@Builder.Default
	private boolean takeOnlyIfStartOnTime = false;
	private int steps;
	private int bonus;

	@Setter(AccessLevel.PROTECTED)
	private long points;

	public void addRide(Ride ride) {
		rides.add(ride);
		//TODO bonus
		points += ride.getTime();
	}

	public boolean canTakeRide(Ride ride) {
		if (rides.isEmpty()) {
			return true;
		} else {
			Ride last = rides.get(rides.size() - 1);
			int end = last.getEarliestStart() + last.getTime() + last.getEnd().squareDistance(ride.getStart()) + ride.getTime();
			if (end > steps) {
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
