package net.collaud.hashcode.data;

import lombok.Data;
import lombok.experimental.Builder;
import net.collaud.hashcode.common.data.Point2DInt;

@Builder
@Data
public class Ride {
	private int id;
	private Point2DInt start;
	private Point2DInt end;
	private int earliestStart;
	private int latestFinish;
}
