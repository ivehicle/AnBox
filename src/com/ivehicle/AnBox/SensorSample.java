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

public class SensorSample {
	
	public static final int LATITUDE = 0;
	public static final int LONGITUDE = 1;
	public static final int VELOCITY = 2;
	public static final int ORIENTATION = 3;
	public static final int THETA_P = 4;
	public static final int THETA_R = 5;
	public static final int ACC_X_VEHICLE = 6;
	public static final int ACC_Y_VEHICLE = 7;
	public static final int ACC_Z_VEHICLE = 8;
	public static final int ACC_X_PHONE = 9;
	public static final int ACC_Y_PHONE = 10;
	public static final int ACC_Z_PHONE = 11;
	public static final int MAG_X = 12;
	public static final int MAG_Y = 13;
	public static final int MAG_Z = 14;
	public static final int VEL_X_VEHICLE = 15;
	public static final int VEL_Y_VEHICLE = 16;
	public static final int VEL_Z_VEHICLE = 17;
	public static final int DIFFACC_X_VEHICLE = 18;
	public static final int DIFFACC_Y_VEHICLE = 19;
	public static final int DIFFACC_Z_VEHICLE = 20;


	public static final int ARRAY_SIZE = 21;
	// hjs
	public static final double WRONG_SENSOR_TYPE = -9999.0;

	private static final int STR_SAMPLED_TIME = 0;
	private static final int STR_LATITUDE = 1;
	private static final int STR_LONGITUDE = 2;
	private static final int STR_VELOCITY = 3;
	private static final int STR_ORIENTATION = 4;
	private static final int STR_THETA_P = 5;
	private static final int STR_THETA_R = 6;
	private static final int STR_ACC_X_VEHICLE = 7;
	private static final int STR_ACC_Y_VEHICLE = 8;
	private static final int STR_ACC_Z_VEHICLE = 9;
	private static final int STR_ACC_X_PHONE = 10;
	private static final int STR_ACC_Y_PHONE = 11;
	private static final int STR_ACC_Z_PHONE = 12;
	private static final int STR_MAG_X = 16;
	private static final int STR_MAG_Y = 17;
	private static final int STR_MAG_Z = 18;

	public long sampledAt;
	public double[] values;
	
	public SensorSample(String line) {
		String[] strVals = line.split(" +");
		sampledAt 				= Long.parseLong(strVals[STR_SAMPLED_TIME]);
		values 					= new double[ARRAY_SIZE];
		values[LATITUDE] 		= Double.parseDouble(strVals[STR_LATITUDE]);
		values[LONGITUDE] 		= Double.parseDouble(strVals[STR_LONGITUDE]);
		values[VELOCITY] 		= Double.parseDouble(strVals[STR_VELOCITY]);
		values[ORIENTATION] 	= Double.parseDouble(strVals[STR_ORIENTATION]);
		values[THETA_P] 		= Double.parseDouble(strVals[STR_THETA_P]);
		values[THETA_R] 		= Double.parseDouble(strVals[STR_THETA_R]);
		values[ACC_X_VEHICLE] 	= Double.parseDouble(strVals[STR_ACC_X_VEHICLE]);
		values[ACC_Y_VEHICLE] 	= Double.parseDouble(strVals[STR_ACC_Y_VEHICLE]);
		values[ACC_Z_VEHICLE] 	= Double.parseDouble(strVals[STR_ACC_Z_VEHICLE]);
		values[ACC_X_PHONE] 	= Double.parseDouble(strVals[STR_ACC_X_PHONE]);
		values[ACC_Y_PHONE] 	= Double.parseDouble(strVals[STR_ACC_Y_PHONE]);
		values[ACC_Z_PHONE] 	= Double.parseDouble(strVals[STR_ACC_Z_PHONE]);
		values[MAG_X] 			= Double.parseDouble(strVals[STR_MAG_X]);
		values[MAG_Y] 			= Double.parseDouble(strVals[STR_MAG_Y]);
		values[MAG_Z] 			= Double.parseDouble(strVals[STR_MAG_Z]);
	}
}
