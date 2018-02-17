package net.collaud.hashcode.common.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * @param <T>
 * @author Gaetan Collaud
 */
public class Matrix<T> {
	@Getter
	protected final List<List<T>> matrix;

	@Getter
	private final int rows;

	@Getter
	private final int columns;

	public Matrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		matrix = new ArrayList<>(rows);
		for (int i = 0; i < rows; i++) {
			final List<T> arrRow = new ArrayList<>(columns);
			matrix.add(arrRow);
		}
	}

	public Matrix(int rows, int columns, T initialValue) {
		this(rows, columns);
		for (int i = 0; i < rows; i++) {
			final List<T> arrRow = matrix.get(i);
			for (int j = 0; j < columns; j++) {
				arrRow.add(initialValue);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("================================\n");
		matrix.forEach(line -> {
			line.forEach(cel -> sb.append(cel).append(" "));
			sb.append('\n');
		});
		return sb.toString();
	}
}
