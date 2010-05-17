package com.ivehicle.AnBox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.ivehicle.util.Log;

public class ShockEventRecorder implements 
	SensorTracker.OnShockEventListener, DataStorageManager.DataRecorder {

	private RandomAccessFile currentShockFile = null;
	private long prevShockTime = 0;
	private boolean recording = false;

	public ShockEventRecorder() {
	}

	public void onShock(ShockEvent shockEvent) {
		if (currentShockFile == null)
			return;

		// A shock will run for Config.getShockRunningTime() at least
		if (System.currentTimeMillis() - prevShockTime < Config.getShockRunningTime())
			return;

		prevShockTime = shockEvent.occurredAt;
		try {
			String line = String.valueOf(shockEvent.occurredAt) + ",";
			if (shockEvent.locString == null || shockEvent.locString == "")
				line += "No location,";
			else
				line += (shockEvent.locString + ",");
			for (int i = 0; i < shockEvent.mLocHistoryX.length; ++i)
				line += String.valueOf(shockEvent.sensorCapturedAt[i]) + ","
						+ String.valueOf(shockEvent.mLocHistoryX[i]) + ","
						+ String.valueOf(shockEvent.mLocHistoryY[i]) + ","
						+ String.valueOf(shockEvent.mLocHistoryZ[i]) + ",";
			line = line.substring(0, line.length()-1);
			line += "\n";
			currentShockFile.write(line.getBytes());
		} catch (IOException e) {
			Log.e(Config.TAG, "Failed to record a shock event - " + e.toString());
		}
	}

	public synchronized String prepareRecording(long dateTaken) {
		Log.d(Config.TAG, toString() + ".prepareRecording()");
		try {
			if (currentShockFile != null)
				currentShockFile.close();

			String fn = Config.getShockFileName(dateTaken);
			currentShockFile = new RandomAccessFile(fn, "rw");
			return fn;
		}
		catch (FileNotFoundException e) {
			Log.e(Config.TAG, e.toString());
		}
		catch (IOException e) {
			Log.e(Config.TAG, e.toString());
		}
		currentShockFile = null;

		Log.d(Config.TAG, toString() + ".prepareRecording(): Returning");
		return null;
	}

	public synchronized void start() {
		Log.d(Config.TAG, toString() + ".start()");
		recording = true;
		Log.d(Config.TAG, toString() + ".start(): Returning");
	}

	public synchronized void stop() {
		try {
			if (currentShockFile != null)
				currentShockFile.close();
		}
		catch (IOException e) {
			Log.e(Config.TAG, e.toString());
		}
		currentShockFile = null;
		recording = false;
	}

	public synchronized boolean isRecording() {
		return recording;
	}
}
