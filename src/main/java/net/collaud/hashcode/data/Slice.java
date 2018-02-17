package net.collaud.hashcode.data;

import lombok.Data;
import lombok.experimental.Builder;

@Builder
@Data
public class Slice {

	private int columnStart;
	private int columnEnd;
	private int rowStart;
	private int rowEnd;

	public void print(StringBuilder sb){
		sb.append(rowStart).append(' ');
		sb.append(columnStart).append(' ');
		sb.append(rowEnd).append(' ');
		sb.append(columnEnd).append('\n');
	}

	public SliceBuilder alter(){
		return Slice.builder()
				.columnStart(columnStart)
				.columnEnd(columnEnd)
				.rowStart(rowStart)
				.rowEnd(rowEnd);
	}
}
