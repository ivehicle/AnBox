/*
 * AnBox, and an Android Blackbox application for the have-not-so-much-money's
 * Copyright (C) 2010 Yoonsoo Kim, Heekuk Lee, Heejin Sohn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ivehicle.AnBox;

import com.ivehicle.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

public class Config {
	
	public final static int AXIS_X = 0;
	public final static int AXIS_Y = 1;
	public final static int AXIS_Z = 2;

	public final static String TAG = "AnBox";
	public final static String DATA_DIR = "/AnBox";
	public final static String FILE_NAME_FORMAT = "yyyyMMddHHmmss";
	public final static String SHOCK_EVENT_FORMAT = "MM-dd HH:mm";
	public final static String SENSOR_DATA_EXT = "dat";
	public final static String SHOCK_EXT = "shk";
	public final static String MOVIE_EXT = "3gp";
	public final static String MOVIE_MIME_TYPE = "video/3gpp";
	public final static int SHK_LENGTH = 20;
	public final static float MAX_SHK_TIME = 200000000;	//ms. period to calculate accMean
	public final static int CAL_TIME = 10*1000;		//calibration time. 10s
	public final static float DEG_TO_RAD = (float)Math.PI/180.0f;
	public final static float RAD_TO_DEG = 180.0f/(float)Math.PI;
	public final static int MAX_VEL_FOR_ORIENTATION_SENSOR = 0;

	private static Context ctx = null;
	private static SharedPreferences pref = null;
	private static StatFs statFs = null;

	private static long maxStorage = 0;
	private static long shockStorage = 0;

	private static float[] calibratedGravities = null;
	private static boolean sendSOSMsg = false;
	private static int sosMsgCancelWaitTime = 0;
	private static String phoneNumberList = null;
	private static String messageToSend = null;
	private static int periodGps=1000;
	private static int distanceGps=0;
	private static long periodAcc=10000000;
	private static int periodSensorFile=100;
	private static int periodAddressUpdate=3000;
	private static float accThreshold = 15;

	public static void initialize(Context context) {
		ctx = context;

		if (statFs == null)
			statFs = new StatFs(Config.getDataDir());

		pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		String prefVal;
		long sizeInMB = 0;
		if (pref.getBoolean("limit_storage", true)) {
			try {
				prefVal = pref.getString("storage_size", "2048");
				sizeInMB = Integer.parseInt(prefVal);
				if (sizeInMB < 100)
					sizeInMB = 100;
			}
			catch (ClassCastException e) {
				Log.e(Config.TAG, e.toString());
				sizeInMB = 512;
			}
			maxStorage = sizeInMB * 1024 * 1024;

		try {
			prefVal = pref.getString("shock_storage_size", "410");
			sizeInMB = Integer.parseInt(prefVal);
			if (sizeInMB < 32)
				sizeInMB = 32;
		}
		catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString());
				sizeInMB = 100;
		}
			shockStorage = sizeInMB * 1024 * 1024;
		}
		else {
			maxStorage = -1;
			shockStorage = 100 * 1024 * 1024;
		}
		
		sendSOSMsg = pref.getBoolean("send_sos_message", false);
		sosMsgCancelWaitTime = Integer.parseInt(
			pref.getString("message_cancel_wait_time", "10"));
		phoneNumberList = pref.getString("number_of_message_to_send", "");
		messageToSend = pref.getString("message_to_send", "An accident may occur at ");

		try {
			prefVal = pref.getString("adv_set_gps", String.valueOf(periodGps));
			periodGps = Integer.parseInt(prefVal);
		} catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString()+"[gps]");
		}
		try {
			prefVal = pref.getString("adv_set_acc", String.valueOf(periodAcc));
			periodAcc = Long.parseLong(prefVal);
		} catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString()+"[acc]");
		}
		
		try {
			prefVal = pref.getString("adv_set_dist", String.valueOf(distanceGps));
			distanceGps = Integer.parseInt(prefVal);
		} catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString()+"[dist]");
		}

		try {
			prefVal = pref.getString("adv_set_sensor_write_period", String.valueOf(periodSensorFile));
			periodSensorFile = Integer.parseInt(prefVal);
		} catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString()+"[sf]");
		}

		try {
			prefVal = pref.getString("adv_set_address", String.valueOf(periodAddressUpdate));
			periodAddressUpdate = Integer.parseInt(prefVal);
		} catch (ClassCastException e) {
			Log.e(Config.TAG, e.toString());
		}
	
		int selection = 3;
		try {
			prefVal = pref.getString("sensor_sensitivity", String.valueOf(selection));
			selection = Integer.parseInt(prefVal);
		} catch (ClassCastException e) {
				Log.e(Config.TAG, e.toString());
		}
		
		switch (selection) {
		case 1:		//very sensitive
			accThreshold = 10;
			break;
		case 2:		// sensitive
			accThreshold = 15;
			break;
		case 3:		// normal
			accThreshold = 20;
			break;
		case 4:		// insensitive
			accThreshold = 25;
			break;
		case 5:		// very insensitive
			accThreshold = 30;
			break;
		default:
			accThreshold = 15;
			break;
		}
	}

	public static String getFormattedShockEvent(long shockTime) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat(SHOCK_EVENT_FORMAT);
    	Date date = new Date(shockTime);
    	return dateFormat.format(date);
	}

	public static long getTimeFromFileName(String fn) {
		int idx = fn.indexOf('.');
		String dateString = fn.substring(0, idx);
		SimpleDateFormat formatter = new SimpleDateFormat(FILE_NAME_FORMAT);
		Date date = formatter.parse(dateString, new ParsePosition(0));
		return date.getTime();
	}

	public static String getDataDir() {
		return Environment.getExternalStorageDirectory().toString() + DATA_DIR;
	}

	public static String getDataDirWithSeparator() {
		return getDataDir() + "/";
	}

	public static String getFileName(long dateTaken) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_FORMAT);
    	Date date = new Date(dateTaken);
    	String filepart = dateFormat.format(date);
		return filepart;		
	}
	
	public static String getMovieFileName(long dateTaken) {
    	String filename = Config.getDataDirWithSeparator() + getFileName(dateTaken) + "." + Config.MOVIE_EXT;
		return filename;
	}
	
	public static String getSensorFileName(long dateTaken) {
    	String filename = Config.getDataDirWithSeparator() + getFileName(dateTaken) + "." + Config.SENSOR_DATA_EXT;
		return filename;	
	}
	
	public static String getShockFileName(long dateTaken) {
    	String filename = Config.getDataDirWithSeparator() + getFileName(dateTaken) + "." + Config.SHOCK_EXT;
		return filename;			
	}

	public static long getAvailableStorage() {
		statFs.restat(Config.getDataDir());
		return (long)statFs.getAvailableBlocks() * statFs.getBlockSize();
	}

	public static long getMaximumStorageForMotionCapture() {
		return maxStorage;
	}

	public static long getMaximumStorageForShockCapture() {
		return shockStorage;
	}

	public static int getCaptureDuration() {
		return 5 * 60 * 1000;					// 5 minutes
	}

	public static long getMinimumStorageLevel() {
		return 16 * 1024 * 1024;				// 16MB --> 5 minutes motion capture
	}

	public static long getMinimumStorageLevelAtReclaiming() {
		return getMinimumStorageLevel() * 2;	// 32MB --> 10 minutes motion capture
	}

	public static int getShockRunningTime() {
		return 20 * 1000;	// 20 seconds
	}

	public static float getMinimumShockLevel() {
		return accThreshold;
	}

	public static int getGpsPeriod() {
		return periodGps;	//ms
	}

	public static long getAccPeriod() {
		return periodAcc;			// ns
	}

	public static int getGpsUpdateDistance() {	// minimum distance for gps update
		return distanceGps;	//m
	}

	public static int getSensorRecordingPeriod() {	// period for recording sensor value
		return periodSensorFile;
	}

	public static int getAddressUpdatePeriod() {
		return periodAddressUpdate;
	}

	public static float[] getCalibratedGravities() {
		pref = PreferenceManager.getDefaultSharedPreferences(ctx);

		if (calibratedGravities == null)
			calibratedGravities = new float[3];
		calibratedGravities[AXIS_X] = pref.getFloat("calibrated_gravity_x", 
			-SensorManager.STANDARD_GRAVITY);
		calibratedGravities[AXIS_Y] = pref.getFloat("calibrated_gravity_y", 
			-SensorManager.STANDARD_GRAVITY);
		calibratedGravities[AXIS_Z] = pref.getFloat("calibrated_gravity_z", 
			-SensorManager.STANDARD_GRAVITY);
		return calibratedGravities;
	}

	public static boolean isCalibrated() {
		pref = PreferenceManager.getDefaultSharedPreferences(ctx);

		return !(pref.getBoolean("need_calibration", true));
	}

	public static boolean getSendSOSMsg() {
		return sendSOSMsg;
	}

	public static int getSOSMsgCancelWaitTime() {
		return sosMsgCancelWaitTime;
	}

	public static String getPhoneNumberList() {
		return phoneNumberList;
	}

	public static String getMessageToSend() {
		return messageToSend;
	}

	public static String getModelName() {
		return android.os.Build.MODEL;
	}

	public static String getVendorName() {
		return android.os.Build.MANUFACTURER;
	}

	public static String getProductName() {
		return android.os.Build.PRODUCT;
	}
}
