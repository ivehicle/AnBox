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

import com.ivehicle.util.Log;
import com.ivehicle.AnBox.Config;
import com.ivehicle.util.FilenameFilterByExtension;

public class SensorSampleIterator {

	private RandomAccessFile currentInputFile;
	private String[] dataFileNames;
	private int currentFileIndex;
	private SensorSample nextSample;

	public SensorSampleIterator(long startsFrom) throws SensorNotCaptured {
		dataFileNames = new File(Config.getDataDir()).list(
			new FilenameFilterByExtension(Config.SENSOR_DATA_EXT));
		
		// 파일들이 생성 날짜 기준으로 오름차순으로 소팅이 되어 있다고 가정
		currentFileIndex = -1;
		for (int i = 0; i < dataFileNames.length; ++i) {
			if (i == 0) {
				if (startsFrom >= Config.getTimeFromFileName(dataFileNames[i]) - 10 * 1000) {
					currentFileIndex = i;
					break;
				}
			}
			else {
				if (startsFrom > Config.getTimeFromFileName(dataFileNames[i])) {
					currentFileIndex = i;
					break;
				}
			}
		}
	
		if (currentFileIndex >= 0) {
			try {
				currentInputFile = new RandomAccessFile(
					Config.getDataDirWithSeparator() + 
					dataFileNames[currentFileIndex], "r");
				nextSample = readNext();
			}
			catch (FileNotFoundException e) {
				Log.e(Config.TAG, dataFileNames[currentFileIndex] + " may be deleted");
				currentInputFile = null;
				nextSample = null;
			}
			catch (IOException e) {
				Log.e(Config.TAG, dataFileNames[currentFileIndex] + " can't be read");
				currentInputFile = null;
				nextSample = null;
			}
		}
		else {
			throw new SensorNotCaptured(startsFrom);
		}
	}
	
	private SensorSample readNext() throws IOException {
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
		SensorSample ret = new SensorSample(line);
		return ret;
	}

	public SensorSample next() {
		SensorSample ret = nextSample;
		try {
			nextSample = readNext();
		} catch (IOException e) {
			return null;
		}
		return ret;
	}

	public boolean hasNext() {
		return (nextSample != null);
	}


}
