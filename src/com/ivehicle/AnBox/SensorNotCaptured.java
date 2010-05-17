package com.ivehicle.AnBox;

@SuppressWarnings("serial")

public class SensorNotCaptured extends RuntimeException {

	public SensorNotCaptured() {
		super("Sensor sample was not captured");
	}
	
	public SensorNotCaptured(final long timeInMs) {
		super("Sensor sample was not captured for time " + timeInMs);
	}
}
