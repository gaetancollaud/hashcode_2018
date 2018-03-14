package net.collaud.hashcode;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.data.Point2DInt;
import net.collaud.hashcode.common.reader.InputReader;
import net.collaud.hashcode.common.utils.PerfUtil;
import net.collaud.hashcode.common.utils.TimerUtil;
import net.collaud.hashcode.data.AutonomousCar;
import net.collaud.hashcode.data.MyMap;
import net.collaud.hashcode.data.Ride;
import net.collaud.hashcode.data.RidePlan;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HashCodeAutonomousCar extends AbstractHashCode {

	protected MyMap map;
	protected List<Ride> rides = new ArrayList<>();
	protected List<AutonomousCar> cars = new ArrayList<>();
	protected AtomicLong bestPoints = new AtomicLong(0);
	protected int nbStep;
	protected int bonus;

	@Builder
	public static class SolutionChoosed{
		List<RidePlan> plans;
		long point;
	}


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
			LOG.error("nbRide={}, rideTaken.siez={}", nbrides, rides);
		}
	}

	protected static List<RidePlan> doSolveOnce(List<Ride> rides, Supplier<RidePlan> newRideSupplier, int carSize) {
		List<RidePlan> ridePlans = new ArrayList<>();
		ridePlans.add(newRideSupplier.get());

		Collections.shuffle(rides);
//		Collections.sort(rides, Comparator.comparing(Ride::getTime).reversed());

		for (int i = 0; i < rides.size(); i++) {
			Ride r = rides.get(i);
			boolean fitInOne = false;
			for (RidePlan rp : ridePlans) {
				if (rp.canTakeRide(r)) {
					fitInOne = true;
					break;
				}
			}
			if (!fitInOne) {
				RidePlan newRp = newRideSupplier.get();
				newRp.canTakeRide(r);
				ridePlans.add(newRp);
			}
		}

		Collections.sort(ridePlans, Comparator.comparing(RidePlan::getPoints).reversed());
		int max = Math.min(carSize, ridePlans.size());

		ridePlans.stream().skip(max - 1).forEach(rp -> {
			ridePlans.stream().limit(max - 1).forEach(rpChoosed -> {
				Iterator<Ride> iterator = rp.getRides().iterator();
				while (iterator.hasNext()) {
					Ride r = iterator.next();
					if (rpChoosed.canTakeRide(r)) {
						iterator.remove();
					}
				}
			});
		});

		return ridePlans;
	}

	@Override
	protected void doSolve() {

		int maxIter = 100;

		SolutionChoosed solution = SolutionChoosed.builder()
				.point(-1)
				.build();

		TimerUtil timer = TimerUtil.start(TimerUtil.TimerConfig.builder()
				.logger(LOG)
				.maxStep(maxIter)
				.build());

		AtomicInteger index = new AtomicInteger(1);

		Stream.iterate(0, n -> n + 1)
				.limit(maxIter)
				.parallel()
				.forEach(i -> {
					List<RidePlan> ridePlans = doSolveOnce(new ArrayList<>(rides), () -> newRidePlan(), cars.size());
					Long points = ridePlans.stream().collect(Collectors.summingLong(r -> r.getPoints()));
					synchronized (solution) {
						if (points > solution.point) {
							solution.plans = ridePlans;
							solution.point = points;
						}
					}
					timer.loop(index.getAndIncrement(), () -> "size: " + ridePlans.size() + ", pts:" + points);
				});


		int max = Math.min(cars.size(), solution.plans.size());
		for (int i = 0; i < max; i++) {
			RidePlan ridePlan = solution.plans.get(i);
			cars.get(i).setAssignedRide(ridePlan.getRides());
		}

		bestPoints.set(solution.point);

		LOG.info("{} rideTaken in {} ride plans, for {} cars", rides.size(), solution.plans.size(), cars.size());
	}

	protected RidePlan newRidePlan() {
		return RidePlan.builder()
				.bonus(bonus)
				.maxSteps(nbStep)
				.build();
	}


	@Override
	protected void writeOutput() {
		writeOutput(sb -> {
			cars.stream().forEach(c -> c.print(sb));
			sb.deleteCharAt(sb.length() - 1);
		});
	}

	@Override
	protected long computePoints() {
		return bestPoints.get();
	}


}
