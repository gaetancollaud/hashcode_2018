package net.collaud.hashcode.data;

import lombok.Data;
import net.collaud.hashcode.common.data.Matrix;

@Data
public class MyMap extends Matrix<AutonomousCar> {
	public MyMap(int rows, int columns) {
		super(rows, columns);
	}
}
