package net.collaud.hashcode.common.utils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TimerUtil {

	public enum TimerMode {
		EACH_PERCENT,
		DELAY,
		STEPS
	}

	@Builder
	public static class TimerConfig {
		private final Logger logger;

		@Builder.Default
		private final long maxStep = -1;

		@Builder.Default
		private final TimerMode mode = TimerMode.EACH_PERCENT;

	}

	private final TimerConfig config;

	private long startTime;
	private long lastStep = 0;
	private int lastPercent = 0;

	protected TimerUtil start() {
		if (this.startTime != 0) {
			throw new RuntimeException("Timer already started at " + this.startTime);
		}
		this.startTime = System.currentTimeMillis();
		return this;
	}

	protected String printDuration(Duration duration) {
		return duration.toString()
				.substring(2)
				.replaceAll("(\\d[HMS])(?!$)", "$1 ")
				.toLowerCase();
	}

	public void loop(long step) {
		loop(step, null);
	}

	synchronized public void loop(long step, Supplier<String> additonalInfo) {
		switch (config.mode) {
			case EACH_PERCENT:
				loopEachPercent(step, Optional.ofNullable(additonalInfo));
				break;
			default:
				config.logger.error("Logger type not implemented yet");
		}
	}

	protected void loopEachPercent(long step, Optional<Supplier<String>> additionalInfo) {
		int percent = getPercentInt(step);
		if (percent > lastPercent) {
			long now = System.currentTimeMillis();
			config.logger.info("{}/{} steps ({}%) in {}, ETA {}: {}", step, config.maxStep, percent, printDuration(getDuration(now)), printDuration(getETA(now, step)), additionalInfo.map(Supplier::get).orElse(""));
			lastStep = step;
			lastPercent = percent;
		}
	}

	protected Duration getDuration(long now) {
		return Duration.ofMillis(now - startTime);
	}

	protected Duration getETA(long now, long step) {
		double duration = now - startTime;
		double byStep = duration / step;
		long stepLeft = config.maxStep - step;
		return Duration.ofMillis(((long) (stepLeft * byStep)));
	}

	protected int getPercentInt(long step) {
		return (int) (getPercent(step));
	}

	protected double getPercent(long step) {
		return 100.0 * step / config.maxStep;
	}

	public static TimerUtil start(TimerConfig config) {
		if (config.logger == null) {
			throw new RuntimeException("Logger must be specified");
		}
		if (config.maxStep == -1) {
			throw new RuntimeException("MaxSteps must be specified");
		}
		return new TimerUtil(config).start();
	}
}
