package net.collaud.hashcode;

import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.data.Point2DInt;
import net.collaud.hashcode.common.reader.InputReader;
import net.collaud.hashcode.common.utils.PerfUtil;
import net.collaud.hashcode.data.AutonomousCar;
import net.collaud.hashcode.data.MyMap;
import net.collaud.hashcode.data.Ride;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HashCodeAutonomousCar extends AbstractHashCode {

	protected MyMap map;
	protected List<Ride> rides = new ArrayList<>();
	protected List<AutonomousCar> cars = new ArrayList<>();
	protected AtomicInteger bestPoints = new AtomicInteger(0);
	protected int nbStep;
	protected int bonus;


	public HashCodeAutonomousCar(String inputFile, String outputFile) {
		super(inputFile, outputFile);
	}

	@Override
	protected void readInput() {
		List<String> lines = getLines();
		List<Integer> params = InputReader.parseNumberInLine(lines.get(0));
		map = new MyMap(params.get(0), params.get(1));
		Integer nbcar = params.get(2);
		Integer nbrides = params.get(3);
		bonus = params.get(4);
		nbStep = params.get(5);
		cars = new ArrayList<>(nbcar);
		rides = new ArrayList<>(nbrides);
		for (int i = 0; i < nbcar; i++) {
			cars.add(AutonomousCar.builder().id(i + 1)
					.currentPosition(new Point2DInt(0, 0))
					.assignedRide(new ArrayList<>())
					.nextStepAvailable(-100)
					.build());
		}

		for (int i = 1; i < lines.size(); i++) {
			String[] line = lines.get(i).split(" ");
			rides.add(Ride.builder()
					.id(i - 1)
					.start(new Point2DInt(Integer.parseInt(line[0]), Integer.parseInt(line[1])))
					.end(new Point2DInt(Integer.parseInt(line[2]), Integer.parseInt(line[3])))
					.earliestStart(Integer.parseInt(line[4]))
					.latestFinish(Integer.parseInt(line[5]))
					.done(false)
					.build());
		}
		if (nbrides != rides.size()) {
			LOG.error("nbRide={}, rides.siez={}", nbrides, rides);
		}
	}

	private int currentStep;

	@Override
	protected void doSolve() {
		for (currentStep = 0; currentStep < nbStep; currentStep++) {
			getAvailableCars().forEach(car -> {
				getAvailableRides(car).findFirst().ifPresent(ride -> {
					car.assignRide(ride);
				});
			});
		}
//		cars.forEach(c -> {
//			LOG.info("car {} has {} rides", c.getId(), c.getAssignedRide().size());
//		});
	}

	protected Stream<AutonomousCar> getAvailableCars() {
		return cars.stream()
				.filter(c -> c.getNextStepAvailable() < currentStep);
	}

	protected Stream<Ride> getAvailableRides(AutonomousCar car) {
		return rides.stream()
				.filter(r -> !r.isDone())
				.filter(r -> r.earlyStartToArriveInTime(car.getCurrentPosition()) <= currentStep)
				.filter(r -> r.canBeTaken(currentStep));
	}


	@Override
	protected void writeOutput() {
		writeOutput(sb -> {
			cars.stream().forEach(c -> c.print(sb));
			sb.deleteCharAt(sb.length() - 1);
		});
	}

	@Override
	protected int computePoints() {
		return bestPoints.get();
	}


}
