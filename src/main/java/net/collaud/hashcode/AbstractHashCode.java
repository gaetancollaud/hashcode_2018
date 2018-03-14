package net.collaud.hashcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.collaud.hashcode.common.reader.InputReader;
import net.collaud.hashcode.common.writer.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Gaetan Collaud
 */
@RequiredArgsConstructor
abstract public class AbstractHashCode {

	protected static final Logger LOG = LoggerFactory.getLogger("App");

	protected String name;
	protected final String inputFile;
	protected final String outputFile;

	public final void solve() {
		long start = System.currentTimeMillis();
		computeName();
		readInput();
		doSolve();
		writeOutput();
		long end = System.currentTimeMillis();

		LOG.info("Solving {} took {}s and gave {}pts", inputFile, ((end - start) / 1000), computePoints());
	}

	protected void computeName() {
		int index = inputFile.lastIndexOf('/');
		int ext = inputFile.lastIndexOf('.');
		this.name = inputFile.substring(index+1, ext);
		Thread.currentThread().setName(this.name);
	}

	protected List<String> getLines() {
		InputReader reader = new InputReader(inputFile);
		return reader.getRawLines();
	}

	protected void writeOutput(Consumer<StringBuilder> consumer) {
		OutputWriter ow = new OutputWriter(outputFile);
		StringBuilder sb = ow.getStringBuilder();
		consumer.accept(sb);
		ow.writeFile();
		//System.out.println(ow.getContent());
	}

	abstract protected void readInput();

	abstract protected void doSolve();

	abstract protected void writeOutput();

	abstract protected long computePoints();
}
