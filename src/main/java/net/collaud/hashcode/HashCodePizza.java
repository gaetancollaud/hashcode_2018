package net.collaud.hashcode;

import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.reader.InputReader;
import net.collaud.hashcode.common.utils.PerfUtil;
import net.collaud.hashcode.data.Pizza;
import net.collaud.hashcode.data.Slice;
import net.collaud.hashcode.data.Topping;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HashCodePizza extends AbstractHashCode {

	protected Pizza pizza;
	protected List<Slice> bestSlices = new ArrayList<>();
	protected AtomicInteger bestPoints = new AtomicInteger(0);

	protected int minIngredient;
	protected int maxIngredient;

	public HashCodePizza(String inputFile, String outputFile) {
		super(inputFile, outputFile);
	}

	@Override
	protected void readInput() {
		List<String> lines = getLines();
		List<Integer> params = InputReader.parseNumberInLine(lines.get(0));
		pizza = new Pizza(params.get(0), params.get(1));
		minIngredient = params.get(2);
		maxIngredient = params.get(3);
		LOG.info("minIngredient: {}, maxIngredient:{}", minIngredient, maxIngredient);


		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			List<Topping> arr = pizza.getMatrix().get(i - 1);
			for (int j = 0; j < line.length(); j++) {
				arr.add(new Topping(line.charAt(j) == 'T'));
			}
		}
	}

	@Override
	protected void doSolve() {
		BiPredicate<Integer, Integer> predicate = (tomato, mushroom) -> tomato >= minIngredient && tomato <= maxIngredient && mushroom >= minIngredient && mushroom <= maxIngredient;
		List<Pair<Integer, Integer>> sliceSize = getSliceSize();

		Object lock = new Object();

		int iteration = 1;
		PerfUtil.parallelIntStream(iteration).forEach(i -> {
			Pizza myPizza = pizza.clone();
			List<Slice> mySlice = solveOnce(myPizza, predicate, sliceSize);
			perfectionne(myPizza, mySlice, predicate);
			int points = myPizza.getPoints();
			LOG.info("[{}] Solutions found with {} slices, score {}pts", i, mySlice.size(), points);
			synchronized (lock) {
				if (bestPoints.get() < points) {
					bestPoints.set(points);
					bestSlices = mySlice;
				}
			}
		});

//		System.out.println(pizza);
	}

	protected boolean sliceAllowed(Slice slice) {
		if (slice.getRowStart() < 0 || slice.getColumnStart() < 0 || slice.getRowEnd() >= pizza.getRows() || slice.getColumnEnd() >= pizza.getColumns()) {
			return false;
		}
		int width = slice.getColumnEnd() - slice.getColumnStart()+1;
		int height = slice.getRowEnd() - slice.getRowStart()+1;
		int cells = width * height;
		return cells >= minIngredient && cells <= maxIngredient;
	}

	protected List<Slice> solveOnce(Pizza myPizza, BiPredicate<Integer, Integer> predicate, List<Pair<Integer, Integer>> sliceSize) {
		List<Slice> mySlices = new ArrayList<>();
		for (int row = 0; row < myPizza.getRows(); row++) {
			for (int col = 0; col < myPizza.getColumns(); col++) {
				List<Slice> slicePossible = getSlicePossible(row, col, sliceSize).collect(Collectors.toList());
//				Collections.shuffle(slicePossible);
				slicePossible.forEach(slice -> {
					if (myPizza.testCut(slice, predicate)) {
//						LOG.info("Slice choosed {}", slice);
						mySlices.add(slice);
						myPizza.cut(slice);
					}
				});
			}
		}
		return mySlices;
	}

	protected void perfectionne(Pizza myPizza, List<Slice> mySlice, BiPredicate<Integer, Integer> predicate) {
		mySlice.forEach(slice -> {
			myPizza.uncut(slice);
			Arrays.asList(slice.alter().columnStart(slice.getColumnEnd() + 1).build(),
					slice.alter().columnStart(slice.getColumnStart() - 1).build(),
					slice.alter().columnStart(slice.getRowStart() - 1).build(),
					slice.alter().columnStart(slice.getRowEnd() + 1).build()).forEach(sliceToTest -> {
				if (sliceAllowed(sliceToTest) && myPizza.testCut(sliceToTest, predicate)) {
//					LOG.error("Room for improvement");
					slice.setRowStart(sliceToTest.getRowStart());
					slice.setRowEnd(sliceToTest.getRowEnd());
					slice.setColumnStart(sliceToTest.getColumnStart());
					slice.setColumnEnd(sliceToTest.getColumnEnd());
				}
			});
			myPizza.cut(slice);
		});
	}

	@Override
	protected void writeOutput() {
		writeOutput(sb -> {
			sb.append(bestSlices.size()).append('\n');
			bestSlices.forEach(s -> s.print(sb));
		});
	}

	@Override
	protected int computePoints() {
		return bestPoints.get();
	}

	protected List<Pair<Integer, Integer>> getSliceSize() {
		List<Pair<Integer, Integer>> list = new ArrayList<>();
		for (int row = 0; row < maxIngredient; row++) {
			for (int col = 0; col < maxIngredient; col++) {
				int cells = (row + 1) * (col + 1);
				if (cells >= minIngredient && cells <= maxIngredient) {
					list.add(new ImmutablePair<>(row, col));
				}
			}
		}
		Comparator<Pair<Integer, Integer>> comparator = Comparator.comparingInt(l -> l.getKey() * l.getValue());
		Collections.sort(list, comparator.reversed());
//		list.forEach(v -> System.out.println("Slice size : " + v.getKey() + "x" + v.getValue()));
		return list;
	}

	protected Stream<Slice> getSlicePossible(int startRow, int startColum, List<Pair<Integer, Integer>> boxPossibility) {
		return boxPossibility.stream()
				.filter(b -> startRow + b.getKey() < pizza.getRows())
				.filter(b -> startColum + b.getValue() < pizza.getColumns())
				.map(b -> Slice.builder()
						.rowStart(startRow)
						.columnStart(startColum)
						.rowEnd(startRow + b.getKey())
						.columnEnd(startColum + b.getValue())
						.build());
	}
}
