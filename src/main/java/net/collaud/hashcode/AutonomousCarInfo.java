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

    public static void Compute(List<Ride> rides, List<AutonomousCar> cars, int bonus, int nbStep) {
        List<RideInfo> ridesInfo = rides.stream().map(e -> new RideInfo(e, 10)).sorted((e1, e2) -> Long.compare(e1.ratio, e2.ratio)).collect(Collectors.toList());
        List<AutonomousCarInfo> carsInfo = cars.stream().map(e -> new AutonomousCarInfo(e, nbStep)).collect(Collectors.toList());
        int lastpercent = 0;
        long start = System.currentTimeMillis();
        long lastEnd = start;
        long lastNbride = rides.size();
        int i = 0;
        while(true) {
            List<AcceptResult> tests = ridesInfo.stream().map(rideInfo -> carsInfo.stream().map(e -> e.canAccept(rideInfo, bonus, 10)).filter(f -> f != null)).flatMap(e -> e).sorted((e1, e2) -> Long.compare(e1.getRatio(), e2.getRatio())).collect(Collectors.toList());
            AcceptResult result = tests.size() == 0 ? null : tests.get(tests.size() - 1);
            if (result == null) {
                break;
            }
            ridesInfo.remove(result.getRideInfo());
            AutonomousCarInfo car = result.apply();
            if(!car.hasAcceptation()) {
                carsInfo.remove(car);
                if(carsInfo.size() == 0) {
                    break;
                }
            }
            i++;
        }
    }

    AutonomousCar car;
    TreeSet<AcceptResult> rides = new TreeSet<>(Comparator.comparing(AcceptResult::getStart));
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

    public void addRide(AcceptResult result, RideInfo ride) {
        rides.add(result);
        car.getAssignedRide().add(ride.ride);
        computeSteps();
    }

    public AcceptResult canAccept(RideInfo ride, int bonus, int lag) {
        AcceptResult bestResult = null;
        for (int i = 0;i < nbPhases;i++) {
            int timeToGo = positions.get(i).squareDistance(ride.getRide().getStart());
            AcceptResult result = null;
            if((ride.start == starts.get(i) + timeToGo && ride.start + ride.distance <= ends.get(i))) {
                // At start
                result = new AcceptResult();
                result.setStart(ride.start);
                result.setTargetPoint(ride.getRide().getEnd());
            } else if((ride.start >= starts.get(i) + timeToGo && ride.start + ride.distance <= ends.get(i))) {
                // To end
                result = new AcceptResult();
                result.setStart(ends.get(i) - ride.distance - timeToGo);
            } else if(ride.end <= ends.get(i) && ride.end - ride.distance - timeToGo >= ride.start) {
                // between
                result = new AcceptResult();
                result.setStart(ride.end - ride.distance - timeToGo);
            }
            if(result != null) {
                result.setRideInfo(ride);
                result.setIndex(i);
                result.setCar(this);
                result.setTargetPoint(ride.getRide().getEnd());
                result.setSourcePoint(positions.get(i));
                result.setEnd(result.start + ride.distance);
                result.setStartPerfect(ride.start == starts.get(i) + timeToGo);
                result.setEndPerfect(ride.start + ride.distance == ends.get(i));
                result.setTimeLost(timeToGo);
                result.setRatio((result.isStartPerfect() ? bonus : 0) - timeToGo + ride.distance);
                if(bestResult == null  || result.ratio > bestResult.ratio) {
                    bestResult = result;
                }
            }
        }
        return bestResult;
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
