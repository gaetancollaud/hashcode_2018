package net.collaud.hashcode.common.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Gaetan Collaud
 */
@RequiredArgsConstructor
@Getter
public class OutputWriter {

	private final String file;
	@Getter
	private final StringBuilder stringBuilder;

	public OutputWriter(String file) {
		this.file = file;
		stringBuilder = new StringBuilder();
	}
	
	public void writeFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.append(getContent());
			fw.flush();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot write file " + file, ex);
		} finally {
			try {
				fw.close();
			} catch (IOException ex) {
				//nothing to do
			}
		}
	}

	public String getContent() {
		return stringBuilder.toString();
	}
}
