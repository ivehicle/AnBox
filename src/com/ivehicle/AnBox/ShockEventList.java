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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import com.ivehicle.util.Log;
import com.ivehicle.util.AndFilter;
import com.ivehicle.util.FilenameFilterByExtension;
import com.ivehicle.util.SizeFilter;

public class ShockEventList extends Vector<ShockEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3061283730675218453L;

	private RandomAccessFile currentInputFile = null;
	private String[] dataFileNames = null;
	private int currentFileIndex = 0;

	public ShockEventList() {
		dataFileNames = new File(Config.getDataDir()).list(
			new AndFilter(
				new FilenameFilterByExtension(Config.SHOCK_EXT),
				new SizeFilter(SizeFilter.OP_GREATER_THAN, 0)
				)
			);

		if (dataFileNames.length <= 0)
			return;

		for (currentFileIndex = 0;
			 currentFileIndex < dataFileNames.length;
			 ++currentFileIndex) {
			try {
				currentInputFile = new RandomAccessFile(
					Config.getDataDirWithSeparator() + 
					dataFileNames[currentFileIndex], "r");
				break;
			}
			catch (FileNotFoundException e) {
				Log.e(Config.TAG, dataFileNames[currentFileIndex] + " may be deleted");
			}
		}

		try {
			ShockEvent shockEvt = null;
			while ((shockEvt = readNext()) != null) {
				add(shockEvt);
			}
		}
		catch (IOException e) {
			if (currentInputFile != null) {
				try {
					currentInputFile.close();
				}
				catch (IOException e1) {
					// Nothing can be done. just ignore
				}
			}

			currentInputFile = null;
			Log.e(Config.TAG, "Shock events can't be read - " + e.toString());
		}
	}
	
	private ShockEvent readNext() throws IOException {
		String line;
		try {
			line = currentInputFile.readLine();
		}
		catch (EOFException e) {
			currentInputFile.close();
			if (++currentFileIndex < dataFileNames.length) {
			    currentInputFile = new RandomAccessFile(
		    		Config.getDataDirWithSeparator() + 
		    		dataFileNames[currentFileIndex], "r");
			    return readNext();
			}
			else {
			    return null;
			}
		}

		if (line == null) {
			currentInputFile.close();
			if (++currentFileIndex < dataFileNames.length) {
			    currentInputFile = new RandomAccessFile(
			    	Config.getDataDirWithSeparator() + 
			    	dataFileNames[currentFileIndex], "r");
			    return readNext();
			}
			else {
			    return null;
			}
		}

		// line을 parsing 한 후, sensor sample 구성하기
		ShockEvent ret = new ShockEvent(line, dataFileNames[currentFileIndex]);
		return ret;
	}

}
