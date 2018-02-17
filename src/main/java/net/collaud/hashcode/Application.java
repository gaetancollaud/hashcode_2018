package net.collaud.hashcode;

import java.util.Arrays;
import java.util.List;
import net.collaud.hashcode.common.writer.ZipSources;

/**
 *
 * @author Gaetan Collaud
 */
public class Application {

	public static void main(String[] args) {
		
		List<String> files = Arrays.asList(
				"1_example",
				"2_small",
				"3_medium",
				"4_big"
				);
		
		files.parallelStream().forEach(f -> {
			String in = "data/inputs/"+f+".in";
			String out = "data/outputs/"+f+".out";
			new HashCodePizza(in, out).solve();
		});


		new Thread(new ZipSources("data/outputs/0_hashcode.zip")
				.addFolder("src")
				.addFile("README.md")
				.addFile("pom.xml")
				.addFile("LICENSE")
				.addFile(".gitignore")
				.getRunnable())
				.start();
		
	}
	
}
