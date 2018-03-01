package net.collaud.hashcode.common.data;

import lombok.Getter;

/**
 *
 * @author Gaetan Collaud
 */
@Getter
public class Point2DInt {
	private final int row;
	private final int column;

	public Point2DInt(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public Point2DInt(Point2DInt pt){
		this.row = pt.row;
		this.column = pt.column;
	}
	
	public int euclidianDistanceCeil(Point2DInt pt){
		int a = row-pt.row;
		int b = column-pt.column;
		return Double.valueOf(Math.ceil(Math.sqrt((a*a+ b*b)))).intValue();
	}

	public int squareDistance(Point2DInt pt){
		int a = row-pt.row;
		int b = column-pt.column;
		return Math.abs(a)+Math.abs(b);
	}
	
}
