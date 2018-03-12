package net.collaud.hashcode;

import lombok.Data;
import net.collaud.hashcode.common.data.Point2DInt;
import net.collaud.hashcode.data.AutonomousCar;
import net.collaud.hashcode.data.Ride;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author jgi
 */
@Data
public class AutonomousCarInfo {

    AutonomousCar car;
    List<AcceptResult> rides = new ArrayList<>();
    List<Integer> starts;
    List<Integer> ends;
    List<Point2DInt> positions = new ArrayList<>();
    private int nbStep;
    private int nbPhases;

    public AutonomousCarInfo(AutonomousCar car, int nbStep) {
        this.car = car;
        starts = new ArrayList<>();
        ends = new ArrayList<>();
        this.nbStep = nbStep;
        computeSteps();
    }

    public void addRide(AcceptResult result) {
        rides.add(result);
        if(result.getNextRide() != null) {
            result.getNextRide().timeLost = result.getNextPositionTimeLost();
        }
        if(rides.size() > 1) {
            rides = rides.stream().sorted(Comparator.comparing(AcceptResult::getStart)).collect(Collectors.toList());
        }
        computeSteps();
    }

    public void reduce() {
        int crtStart = 0;
        for (int i = 0; i < rides.size(); i++) {
            AcceptResult ride = rides.get(i);
            if(!ride.isStartPerfect()) {
                ride.start = crtStart;
                ride.setEnd(ride.start + ride.getRideInfo().distance + ride.timeLost);
            }
            crtStart = ride.end;
        }
    }

    public AcceptResult canAccept(RideInfo ride, int bonus, int timeToGoPenality, int ridesCountPenality, boolean prefereBonus) {
        AcceptResult bestResult = null;
        for (int i = 0;i < nbPhases;i++) {
            int crtStart = starts.get(i);
            int crtEnd = ends.get(i);
            int timeToGo = positions.get(i).squareDistance(ride.getRide().getStart());
            int nextPositionTimeLost = 0;
            Point2DInt targetPosition = positions.size() > i + 1 ? positions.get(i + 1) : null;
            AcceptResult nextRide = positions.size() > i + 1 ? rides.get(i) : null;
            if(targetPosition != null) {
                nextPositionTimeLost = targetPosition.squareDistance(ride.getRide().getEnd());
                crtEnd = ends.get(i) - nextPositionTimeLost;
            }
            Integer start;
            int additionalBonus = 0;
            if((ride.start == crtStart + timeToGo && ride.start + ride.distance <= crtEnd)) {
                start = ride.start - timeToGo;
                additionalBonus = bonus + ride.distance;
            } else if((ride.start > crtStart + timeToGo && ride.start + ride.distance <= crtEnd)) {
                start = ride.start - timeToGo;
            } else if(!prefereBonus && (ride.start >= crtStart + timeToGo && ride.start + ride.distance <= crtEnd)) {
                start = crtEnd - ride.distance - timeToGo;
                //start = crtStart;
                //start = ride.start - timeToGo;
            } else if(!prefereBonus && (ride.end <= crtEnd && ride.end - ride.distance - timeToGo >= crtStart)) {
                start = ride.end - ride.distance - timeToGo;
                //start = crtStart;
                //start = ride.start - timeToGo;
            } else {
                start = null;
            }
            //List<AcceptResult> ridesAfter = rides.stream().filter(r -> r.start > result.start).collect(Collectors.toList());
            if(start != null) {
                if(rides.size() > 0){
                    int value = 0;
                }
                AcceptResult result = new AcceptResult();
                result.setRideInfo(ride);
                result.setIndex(i);
                result.setCar(this);
                result.setStart(start);
                result.setTargetPoint(ride.getRide().getEnd());
                result.setSourcePoint(positions.get(i));
                result.setEnd(result.start + ride.distance + timeToGo);
                result.setStartPerfect(ride.start == start + timeToGo);
                result.setEndPerfect(ride.start + ride.distance == ends.get(i));
                result.setTimeLost(timeToGo);
                result.setNextPositionTimeLost(nextPositionTimeLost);
                result.setNextRide(nextRide);
                result.setPoints((result.isStartPerfect() ? bonus : 0) + ride.distance);
                result.setRatio(result.points + additionalBonus - (timeToGo * timeToGoPenality) - (rides.size() * ridesCountPenality));
                /*if(!check(result)) {
                    canAccept(ride, bonus, timeToGoPenality, ridesCountPenality, prefereBonus);
                }*/
                if(bestResult == null  || result.ratio > bestResult.ratio) {
                    bestResult = result;
                }
            }
        }
        return bestResult;
    }

    private boolean check(AcceptResult ride) {
        boolean isValid = false;
        for (int j = 0;j < nbPhases;j++) {
            int crtStart2 = starts.get(j);
            int crtEnd2 = ends.get(j);
            if (ride.start >= crtStart2 && ride.end <= crtEnd2) {
                isValid = true;
            }
        }
        if(!isValid) {
            int value = 0;
        }
        return isValid;
    }

    public boolean hasAcceptation() {
        return nbPhases != 0;
    }

    private void computeSteps() {
        starts.clear();
        ends.clear();
        positions.clear();

        int crtStep = 0;
        Point2DInt crtPoint = new Point2DInt(0,0);
        for (AcceptResult ride : rides) {
            if(ride.getStart() != crtStep) {
                starts.add(crtStep);
                ends.add(ride.getStart());
                positions.add(crtPoint);
            }
            crtStep = ride.getEnd();
            crtPoint = ride.targetPoint;
        }
        if(crtStep != nbStep) {
            starts.add(crtStep);
            ends.add(nbStep);
            positions.add(crtPoint);
        }
        nbPhases = starts.size();
    }
}
