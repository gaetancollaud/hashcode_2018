package net.collaud.hashcode.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PerfUtil {

	public static Stream<Integer> parallelIntStream(int max) {
		List<Integer> list = new ArrayList<>(max);
		for (int i = 0; i < max; i++) {
			list.add(i);
		}
		return list.parallelStream();
	}
}
