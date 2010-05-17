package com.ivehicle.AnBox;

import java.util.Comparator;

import com.ivehicle.util.Log;

public class ShockEvent {

	public String containingFile = null;
	public long occurredAt = 0;
	public long[] sensorCapturedAt = null;
	public double[] mLocHistoryX = null;
	public double[] mLocHistoryY = null;
	public double[] mLocHistoryZ = null;
	public String locString;
	public String addr;

	public static class ReverseComparator implements Comparator<ShockEvent> {

		public int compare(ShockEvent lhs, ShockEvent rhs) {
			return (int)(rhs.occurredAt - lhs.occurredAt);
		}

	}

	public ShockEvent(String line, String file) {
		containingFile = file;
		String[] strVals = line.split(",");
		if (strVals.length < Config.SHK_LENGTH * 4 + 2) {
			Log.e(Config.TAG, "Invalid data = " + line);
			return;
		}

		sensorCapturedAt		= new long[Config.SHK_LENGTH];
		mLocHistoryX			= new double[Config.SHK_LENGTH];
		mLocHistoryY			= new double[Config.SHK_LENGTH];
		mLocHistoryZ			= new double[Config.SHK_LENGTH];

		occurredAt 				= Long.parseLong(strVals[0]);
		locString				= strVals[1];
		try {
			for (int i = 0; i < sensorCapturedAt.length; ++i) {
				sensorCapturedAt[i] 	= Long.parseLong(strVals[i*4+2]);
				mLocHistoryX[i] 		= Double.parseDouble(strVals[i*4+3]);
				mLocHistoryY[i] 		= Double.parseDouble(strVals[i*4+4]);
				mLocHistoryZ[i] 		= Double.parseDouble(strVals[i*4+5]);
			}
		}
		catch (NumberFormatException e) {
			Log.e(Config.TAG, "Invalid data format for file " + file);
			Log.e(Config.TAG, e.toString());
		}
	}

	public ShockEvent(long occurredAt) {
		this.occurredAt = occurredAt;
	}

	public String toString() {
		/*
		long currentTime = System.currentTimeMillis();
		long timeDiff = currentTime - occurredAt;

		// Converts to minutes
		timeDiff /= 60 * 1000;
		if (timeDiff == 0)
			return "Right before";
		else if (1 <= timeDiff && timeDiff <= 59)
			return String.valueOf(timeDiff) + "m ago";

		timeDiff /= 60;		// Converts to hours
		if (1 <= timeDiff && timeDiff <= 23)
			return String.valueOf(timeDiff) + "h ago";
		
		timeDiff /= 24;		// Converts to days
		return String.valueOf(timeDiff) + "d ago";
		*/
		return Config.getFormattedShockEvent(occurredAt);
	}

	public String getMovieFilePath() {
		String[] namesSplit = containingFile.split("\\.");
		return Config.getDataDirWithSeparator() + namesSplit[0] + "." + Config.MOVIE_EXT;
	}
}
