package net.collaud.hashcode;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.data.AutonomousCar;
import net.collaud.hashcode.data.Ride;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jgi
 */
@UtilityClass
@Slf4j
public class JgiAlgo {
    public void Compute(List<Ride> rides, List<AutonomousCar> cars, int bonus, int nbStep) {
        List<RideInfo> ridesInfo = rides.stream().map(e -> new RideInfo(e, 10)).sorted(Comparator.comparingLong(e -> e.start)).collect(Collectors.toList());

        long availablePoints1 = ridesInfo.stream().mapToLong(e -> bonus + e.distance).sum();
        long availablePoints2 = ridesInfo.stream().mapToLong(e -> e.distance).sum();
        int timeToGoPenality = 1;
        int ridesCountPenality = 1;

        long crtBestPoint = 0;
        List<AutonomousCarInfo> resultCarsInfo = null;
        for (int i = 0; i < 100;i++) {
            List<AutonomousCarInfo> carsInfo = cars.stream().map(e -> new AutonomousCarInfo(e, nbStep)).collect(Collectors.toList());
            List<AutonomousCarInfo> allCarsInfo = new ArrayList<>(carsInfo);
            ComputeInfos(ridesInfo, carsInfo, bonus * i, nbStep, timeToGoPenality, ridesCountPenality);
            List<RideInfo> usedRides = allCarsInfo.stream().map(e -> e.rides.stream().map(AcceptResult::getRideInfo)).flatMap(e -> e).collect(Collectors.toList());
            List<RideInfo> unusedRides = ridesInfo.stream().filter(r -> !usedRides.contains(r)).collect(Collectors.toList());
            long point = allCarsInfo.stream().map(e -> e.rides.stream()).flatMap(e -> e).mapToLong(e -> (e.isStartPerfect() ? bonus : 0) + e.getRideInfo().distance).sum();

            LOG.info("Solved {} by JgiAlgo : {} pts", i, point);
            if(point > crtBestPoint) {
                resultCarsInfo = allCarsInfo;
                crtBestPoint = point;
            }
        }
        /*ridesInfo = ridesInfo.stream().sorted((e1, e2) -> Long.compare(e1.end, e2.end) * -1).collect(Collectors.toList());
        List<AutonomousCarInfo> carsInfo = cars.stream().map(e -> new AutonomousCarInfo(e, nbStep)).collect(Collectors.toList());
        List<AutonomousCarInfo> allCarsInfo = new ArrayList<>(carsInfo);
        Compute2(ridesInfo, carsInfo, bonus, nbStep);
        List<RideInfo> usedRides = allCarsInfo.stream().map(e -> e.rides.stream().map(AcceptResult::getRideInfo)).flatMap(e -> e).collect(Collectors.toList());
        List<RideInfo> unusedRides = ridesInfo.stream().filter(r -> !usedRides.contains(r)).collect(Collectors.toList());
        long point = allCarsInfo.stream().map(e -> e.rides.stream()).flatMap(e -> e).mapToLong(e -> e.points).sum();

        if(point > crtBestPoint) {
            resultCarsInfo = allCarsInfo;
            crtBestPoint = point;
            LOG.info("Solved by JgiAlgo : {} pts", point);
        }*/

        LOG.info("Solved by JgiAlgo : {} pts", crtBestPoint);

        int count = 0;
        for (AutonomousCarInfo carInfo : resultCarsInfo) {
            boolean hasAlready = false;
            for (AcceptResult result : carInfo.rides) {
                if(result.end >= result.getRideInfo().ride.getLatestFinish() && !hasAlready) {
                    count+= (result.isStartPerfect() ? bonus : 0) + result.getRideInfo().distance;
                    hasAlready = true;
                }
                carInfo.car.getAssignedRide().add(result.getRideInfo().ride);
            }
        }
        List<AcceptResult> tmp = resultCarsInfo.get(0).rides.stream().filter(l -> l.isStartPerfect()).collect(Collectors.toList());
        count = count;
    }

    private static void ComputeInfos(List<RideInfo> rides, List<AutonomousCarInfo> cars, int bonus, int nbStep, int timeToGoPenality, int ridesCountPenality) {
        int nbParts = Math.max(1, Math.min(cars.size(), rides.size()));
        long start = System.currentTimeMillis();
        long lastEnd = start;
        int i = 0;
        /*long lastNbride = rides.size();
        for (currentStep = 0; currentStep < nbStep; currentStep++) {
            int percent = 100 * currentStep / nbStep;
            if (percent != lastpercent) {
                long time = (long) ((System.currentTimeMillis() - start) * 0.001);
                long deltaTime = time - lastEnd;
                lastEnd = time;
                LOG.info("step {}/{} ({}%) in {}s (delta={}), rideLeft={} (delta={})", currentStep, nbStep, percent, time, deltaTime, rides.size(), lastNbride - rides.size());
                lastNbride = rides.size();
                lastpercent = percent;
            }
        }*/
        int skipCount;
        boolean useBonus = false;
        for (int j = useBonus ? 0 : 1;j < 2;j++) {
            boolean prefereBonus = j == 0;
            while (rides.size() > (skipCount = nbParts * i) && cars.size() > 0) {
                int takeCount = nbParts;
                List<RideInfo> crtRides = rides.stream().skip(skipCount).limit(takeCount).collect(Collectors.toList());
                List<AutonomousCarInfo> crtCars = new ArrayList<>(cars);
                List<AcceptResult> tests = getSortedResult(getResults(cars, crtRides, bonus, timeToGoPenality, ridesCountPenality, prefereBonus));
                while(true) {
                    AcceptResult result = tests.size() == 0 ? null : tests.get(0);
                    if (result == null) {
                        break;
                    }
                    crtRides.remove(result.getRideInfo());

                    AutonomousCarInfo car = result.apply();
                    tests = tests.stream().filter(t -> t.getRideInfo().ride != result.getRideInfo().ride && t.getCar().car != result.getCar().car).collect(Collectors.toList());
                    List<AcceptResult> crtCarTests = getResults(Collections.singletonList(car), crtRides, bonus, timeToGoPenality, ridesCountPenality, prefereBonus).collect(Collectors.toList());
                    if(crtCarTests.size() == 0) {
                        crtCars.remove(car);
                        if(crtCars.size() == 0) {
                            break;
                        }
                    } else {
                        tests = getSortedResult(Stream.concat(tests.stream(), crtCarTests.stream()));
                    }
                }
                cars = cars.stream().filter(AutonomousCarInfo::hasAcceptation).collect(Collectors.toList());
                for (AutonomousCarInfo car : cars) {
                    car.reduce();
                }
                i++;
            }
        }
        /*
        int skipCount;
        while (rides.size() > (skipCount = nbParts * i) && cars.size() > 0) {
            int takeCount = nbParts;
            List<RideInfo> crtRides = rides.stream().skip(skipCount).limit(takeCount).collect(Collectors.toList());
            List<AutonomousCarInfo> crtCars = new ArrayList<>(cars);
            List<AcceptResult> tests = getSortedResult(getResults(cars, crtRides, bonus, timeToGoPenality, ridesCountPenality, true));
            while(true) {
                AcceptResult result = tests.size() == 0 ? null : tests.get(0);
                if (result == null) {
                    break;
                }
                crtRides.remove(result.getRideInfo());
                if(result.isStartPerfect()) {
                    LOG.info("ok");
                }

                tests = tests.stream().filter(t -> t.getRideInfo().ride != result.getRideInfo().ride && t.getCar().car != result.getCar().car).collect(Collectors.toList());
                AutonomousCarInfo car = result.apply();
                List<AcceptResult> crtCarTests = getResults(Collections.singletonList(car), crtRides, bonus, timeToGoPenality, ridesCountPenality, true).collect(Collectors.toList());
                if(crtCarTests.size() == 0) {
                    crtCars.remove(car);
                    if(crtCars.size() == 0) {
                        break;
                    }
                } else {
                    tests = getSortedResult(Stream.concat(tests.stream(), crtCarTests.stream()));
                }
            }
            cars = cars.stream().filter(AutonomousCarInfo::hasAcceptation).collect(Collectors.toList());
            i++;
        }
         */
    }
    public static void Compute2(List<RideInfo> ridesInfo, List<AutonomousCarInfo> carsInfo, int bonus, int nbStep) {
        int lastpercent = 0;
        long start = System.currentTimeMillis();
        long lastEnd = start;
        long lastNbride = ridesInfo.size();
        int i = 0;
        while(true) {
            List<AcceptResult> tests = ridesInfo.stream().map(rideInfo -> carsInfo.stream().map(e -> e.canAccept(rideInfo, bonus, 10, 0, false)).filter(f -> f != null)).flatMap(e -> e).sorted((e1, e2) -> Long.compare(e1.getRatio(), e2.getRatio())).collect(Collectors.toList());
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


    private static List<AcceptResult> getSortedResult(Stream<AcceptResult> results) {
        return results.sorted((e1, e2) -> Long.compare(e1.getRatio(), e2.getRatio()) * -1).collect(Collectors.toList());
    }

    private static Stream<AcceptResult> getResults(List<AutonomousCarInfo> cars, List<RideInfo> rides, int bonus, int timeToGoPenality, int ridesCountPenality, boolean prefereBonus) {
        return cars.stream().map(car -> rides.stream().map(ride -> car.canAccept(ride, bonus, timeToGoPenality, ridesCountPenality, prefereBonus)).filter(Objects::nonNull)).flatMap(e -> e);
    }
}
