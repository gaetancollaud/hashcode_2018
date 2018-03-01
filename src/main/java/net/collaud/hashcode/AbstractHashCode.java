package net.collaud.hashcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.reader.InputReader;
import net.collaud.hashcode.common.writer.OutputWriter;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Gaetan Collaud
 */
@Slf4j
@RequiredArgsConstructor
abstract public class AbstractHashCode {

	protected final String inputFile;
	protected final String outputFile;

	public final void solve() {
		long start = System.currentTimeMillis();
		readInput();
		doSolve();
		writeOutput();
		long end = System.currentTimeMillis();

		LOG.info("Solving {} took {}s and gave {}pts", inputFile, ((end - start) / 1000), computePoints());
	}

	protected List<String> getLines() {
		InputReader reader = new InputReader(inputFile);
		return reader.getRawLines();
	}

	protected void writeOutput(Consumer<StringBuilder> consumer) {
		OutputWriter ow = new OutputWriter(outputFile);
		StringBuilder sb = ow.getStringBuilder();
		consumer.accept(sb);
			sb.deleteCharAt(sb.length() - 1);
		ow.writeFile();
		//System.out.println(ow.getContent());
	}

	abstract protected void readInput();

	abstract protected void doSolve();

	abstract protected void writeOutput();

	abstract protected int computePoints();
}
