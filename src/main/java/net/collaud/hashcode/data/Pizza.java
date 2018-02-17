package net.collaud.hashcode.data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.data.Matrix;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Slf4j
public class Pizza extends Matrix<Topping> {

	@Getter
	public int points = 0;

	public Pizza(int rows, int columns) {
		super(rows, columns);
	}

	public void cut(Slice slice) {
		forSlice(slice, (topping) -> {
			if (topping.isUsed()) {
				throw new RuntimeException("Cannot cut slice " + slice + " because it's already been cut");
			} else {
//					LOG.info("Set {}x{} to used", row, col);
				topping.setUsed(true);
				points++;
			}
		});
	}

	public void uncut(Slice slice) {
		forSlice(slice, (topping) -> {
			if (!topping.isUsed()) {
				throw new RuntimeException("Cannot uncut slice " + slice + " because it's already been uncut");
			} else {
//				LOG.info("Set {}x{} to used", row, col);
				topping.setUsed(false);
				points--;
			}
		});
	}

	protected void forSlice(Slice slice, Consumer<Topping> consumer) {

		for (int row = slice.getRowStart(); row <= slice.getRowEnd(); row++) {
			for (int col = slice.getColumnStart(); col <= slice.getColumnEnd(); col++) {
				consumer.accept(matrix.get(row).get(col));
			}
		}
	}

	public boolean testCut(Slice slice, BiPredicate<Integer, Integer> predicate) {
		int tomato = 0;
		int mushroom = 0;
		for (int row = slice.getRowStart(); row <= slice.getRowEnd(); row++) {
			for (int col = slice.getColumnStart(); col <= slice.getColumnEnd(); col++) {
				Topping topping = matrix.get(row).get(col);
				if (topping.isUsed()) {
					return false;
				} else {
					if (topping.isTomato()) {
						tomato++;
					} else {
						mushroom++;
					}
				}
			}
		}
		return predicate.test(tomato, mushroom);
	}

	public void reset() {
		matrix.forEach(row -> row.forEach(cell -> cell.setUsed(false)));
	}

	public Pizza clone() {
		Pizza p = new Pizza(this.getRows(), this.getColumns());
		for (int i = 0; i < getRows(); i++) {
			List<Topping> line = p.matrix.get(i);
			matrix.get(i).forEach(cell -> {
				line.add(cell.clone());
			});
		}
		return p;
	}

}
