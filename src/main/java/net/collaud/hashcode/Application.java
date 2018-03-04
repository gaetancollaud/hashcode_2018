package net.collaud.hashcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.collaud.hashcode.common.writer.ZipSources;

/**
 * @author Gaetan Collaud
 */
public class Application {

	public static void main(String[] args) {

		List<String> files = new ArrayList<>();
		files.add("1_example");
		files.add("2_should_be_easy");
		files.add("3_no_hurry");
		files.add("4_metropolis");
		files.add("5_high_bonus");


		files.parallelStream().forEach(f -> {
			String in = "data/inputs/" + f + ".in";
			String out = "data/outputs/" + f + ".out";
			new HashCodeAutonomousCar(in, out).solve();
		});

		new ZipSources("data/outputs/0_hashcode.zip")
				.addFolder("src")
				.addFile("README.md")
				.addFile("pom.xml")
				.addFile("LICENSE")
				.addFile(".gitignore")
				.getRunnable().run();


	}

}
