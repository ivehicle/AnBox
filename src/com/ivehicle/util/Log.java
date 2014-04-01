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

package com.ivehicle.util;

import com.ivehicle.util.LogConfig;

public class Log {
	public static void e(String tag, String msg) {
		android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		android.util.Log.e(tag, msg, tr);
	}

	public static void w(String tag, String msg) {
		android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable tr) {
		android.util.Log.w(tag, msg, tr);
	}

	public static void i(String tag, String msg) {
		android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable tr) {
		android.util.Log.i(tag, msg, tr);
	}

	public static void d(String tag, String msg) {
		if (LogConfig.DEBUG)
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (LogConfig.DEBUG)
			android.util.Log.d(tag, msg, tr);
	}

	public static void v(String tag, String msg) {
		if (LogConfig.DEBUG)
			android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (LogConfig.DEBUG)
			android.util.Log.v(tag, msg, tr);
	}
}
