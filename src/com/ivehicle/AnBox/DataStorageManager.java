package com.ivehicle.AnBox;

import com.ivehicle.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.ivehicle.util.FilenameFilterByShock;
import com.ivehicle.util.NotFilter;

import android.os.Handler;

public class DataStorageManager implements Runnable {

	public interface DataRecorder {
		public String prepareRecording(long timeInMs);
		public void start();
		public void stop();
		public boolean isRecording();
	}

	private Vector<DataRecorder> recorders = new Vector<DataRecorder>();
	private Handler hdlr = new Handler();
	private LinkedList<File> alreadyRecordedDataFiles = new LinkedList<File>();
	private Vector<File> beingRecordedFiles = new Vector<File>();
	private LinkedList<File> alreadyRecordedShockFiles = new LinkedList<File>();
	private File dataDir = new File(Config.getDataDir());
	private long occupiedDataStorage = 0;
	private long occupiedShockStorage = 0;
	private long beingOccupiedDataStorage = 0;
	private long beingOccupiedShockStorage = 0;
	private long recordingStarted = 0;
	private boolean shockRunning = false;
	private int shockHappened = 0;

	public void registerDataRecorder(DataRecorder recorder) {
		recorders.add(recorder);
	}

	public void unregisterDataRecorder(DataRecorder recorder) {
		recorders.remove(recorder);
	}

	private void scanDataDir() {
		Log.d(Config.TAG, toString() + ".scanDataDir()");
		String[] fileNames = dataDir.list(
			new NotFilter(new FilenameFilterByShock()));
		occupiedDataStorage = 0;
		if (fileNames.length > 0) {
			Arrays.sort(fileNames);
			for (String fn : fileNames) {
				File f = new File(Config.getDataDirWithSeparator() + fn);
				alreadyRecordedDataFiles.add(f);
				occupiedDataStorage += f.length();
			}
		}

		fileNames = dataDir.list(new FilenameFilterByShock());
		occupiedShockStorage = 0;
		if (fileNames.length > 0) {
			Arrays.sort(fileNames);
			for (String fn : fileNames) {
				File f = new File(Config.getDataDirWithSeparator() + fn);
				alreadyRecordedShockFiles.add(f);
				occupiedShockStorage += f.length();
			}
		}
		Log.d(Config.TAG, toString() + ".scanDataDir(): Returning");
	}

	public DataStorageManager() {
		File dataDir = new File(Config.getDataDir());
		dataDir.mkdirs();
		scanDataDir();
	}

	public void run() {
		Log.d(Config.TAG, toString() + ".run()");
		checkDataStorage();
		checkShockStorage();
		Log.d(Config.TAG, toString() + ".run(): after checking storage");

		// process shock event: DO NOT change data file under a shock
		if (shockRunning)
			return;

		if (System.currentTimeMillis() - recordingStarted > Config.getCaptureDuration()) {
			int i = 0;
			recordingStarted = System.currentTimeMillis();
			String[] newFileNames = new String[recorders.size()];
			for (DataRecorder r : recorders) {
				newFileNames[i++] = r.prepareRecording(recordingStarted);
			}
			for (File f : beingRecordedFiles) {
				if (f == null)
					continue;

				if (shockHappened > 0) {
					alreadyRecordedShockFiles.add(f);
					occupiedShockStorage += f.length();
				}
				else {
					alreadyRecordedDataFiles.add(f);
					occupiedDataStorage += f.length();
				}
				shockHappened = 0;
			}
			beingRecordedFiles.clear();
			for (String fn : newFileNames) {
				if (fn != null)
					beingRecordedFiles.add(new File(fn));
				else {
					beingRecordedFiles.add(null);
					Log.w(Config.TAG, "Recorder returned null file name");
				}
			}
		}

		hdlr.postDelayed(this, 1000);
		Log.d(Config.TAG, toString() + ".run(): Returning");
	}

	public void prepare() {
		Log.d(Config.TAG, toString() + ".prepare()");
		recordingStarted = System.currentTimeMillis();
		for (DataRecorder r : recorders) {
			String fn = r.prepareRecording(recordingStarted);
			if (fn != null)
				beingRecordedFiles.add(new File(fn));
			else {
				beingRecordedFiles.add(null);
				Log.w(Config.TAG, "Recorder returned null file name");
			}
		}
		Log.d(Config.TAG, toString() + ".prepare(): Returning");
	}

	public void start() {
		Log.d(Config.TAG, toString() + ".start()");
		for (DataRecorder r : recorders) {
			r.start();
		}
		run();
		Log.d(Config.TAG, toString() + ".start(): Returning");
	}

	public void stop() {
		hdlr.removeCallbacksAndMessages(null);
		for (DataRecorder r : recorders) {
			r.stop();
		}
		for (File f : beingRecordedFiles) {
			if (f == null)
				continue;

			if (shockHappened > 0) {
				alreadyRecordedShockFiles.add(f);
				occupiedShockStorage += f.length();
			}
			else {
				alreadyRecordedDataFiles.add(f);
				occupiedDataStorage += f.length();
			}
			shockHappened = 0;
		}
		beingRecordedFiles.clear();
	}

	private void checkDataStorage() {
		beingOccupiedDataStorage = occupiedDataStorage;
		for (File f : beingRecordedFiles) {
			if (f != null)
				beingOccupiedDataStorage += f.length();
		}

		long availSize = 0;
		long configSize = Config.getMaximumStorageForMotionCapture();
		if (configSize < 0)
			availSize = Config.getAvailableStorage();
		else
			availSize = configSize - beingOccupiedDataStorage;

		if (availSize > Config.getMinimumStorageLevel()) {
			Log.i(Config.TAG, 
				"Storage capacity is enough, occupied = " + beingOccupiedDataStorage +
				", maximum = " + configSize +
				", available = " + availSize);
		}
		else {
			Log.w(Config.TAG, 
				"Out of storage: occupied = " + beingOccupiedDataStorage +
				", maximum = " + configSize +
				", available = " + availSize);
			Log.d(Config.TAG, "Reclaiming data storage...");
			try {
				reclaimDataStorage();
			}
			catch (Exception e) {
				Log.e(Config.TAG, e.toString());
			}
		}
	}

	private void reclaimDataStorage() {
		// At first, reclaim storage from normal data
		long maxSize = Config.getMaximumStorageForMotionCapture();
		try {
			while (alreadyRecordedDataFiles.size() > 0) {
				long deletedStorage = deleteRelatedDataAtFirst(alreadyRecordedDataFiles);
				beingOccupiedDataStorage -= deletedStorage;
				occupiedDataStorage -= deletedStorage;

				long availSize = 0;
				long configSize = Config.getMaximumStorageForMotionCapture();
				if (configSize < 0)
					availSize = Config.getAvailableStorage();
				else
					availSize = configSize - beingOccupiedDataStorage;

				if (availSize > Config.getMinimumStorageLevelAtReclaiming()) {
					Log.d(Config.TAG, 
						"Data storage reclaimed: occupied = " + beingOccupiedDataStorage +
						", maximum = " + maxSize);
					break;
				}
				else {
					Log.d(Config.TAG,
						"Still out of data storage: occupied = " + beingOccupiedDataStorage +
						", maximum = " + maxSize);
				}
			}
		}
		catch (NoSuchElementException e) {
			Log.i(Config.TAG, "Now history data is empty");
			occupiedDataStorage = 0;
		}
	}

	private void checkShockStorage() {
		beingOccupiedShockStorage = occupiedShockStorage;
		if (shockHappened > 0) {
			for (File f : beingRecordedFiles) {
				if (f != null)
					beingOccupiedShockStorage += f.length();
			}
		}

		long availSize = Config.getMaximumStorageForShockCapture() - beingOccupiedShockStorage;
		if (availSize > Config.getMinimumStorageLevel()) {
			Log.i(Config.TAG,
				"Shock storage is enough, occupied = " + beingOccupiedShockStorage +
				", maximum = " + Config.getMaximumStorageForShockCapture());
		}
		else {
			Log.w(Config.TAG, 
					"Out of shock storage: occupied = " + beingOccupiedShockStorage +
					", maximum = " + Config.getMaximumStorageForShockCapture());
			Log.d(Config.TAG, "Reclaiming shock data storage...");
			try {
				reclaimShockStorage();
			}
			catch (Exception e) {
				Log.e(Config.TAG, e.toString());
			}
		}
	}

	private void reclaimShockStorage() {
		try {
			while (alreadyRecordedShockFiles.size() > 0) {
				long deletedStorage = deleteRelatedDataAtFirst(alreadyRecordedShockFiles);
				beingOccupiedShockStorage -= deletedStorage;
				occupiedShockStorage -= deletedStorage;
				long availSize = Config.getMaximumStorageForShockCapture() - beingOccupiedShockStorage;
				if (availSize > Config.getMinimumStorageLevel()) {
					Log.d(Config.TAG, 
						"Shock storage reclaimed: occupied = " + beingOccupiedShockStorage +
						", maximum = " + Config.getMaximumStorageForShockCapture());
					break;
				}
				else {
					Log.d(Config.TAG,
						"Still out of shock storage: occupied = " + beingOccupiedShockStorage +
						", maximum = " + Config.getMaximumStorageForShockCapture());
				}
			}
		}
		catch (NoSuchElementException e) {
			Log.i(Config.TAG, "Now shock history data is empty");
			occupiedShockStorage = 0;
		}
	}

	private long deleteRelatedDataAtFirst(LinkedList<File> list) {
		// Assumption: list is sorted in ascending order
		long deletedStorage = 0;

		File f = list.element();
		long len = f.length();
		f.delete();
		list.remove(f);
		deletedStorage += len;
		Log.d(Config.TAG, f.getPath() + " deleted");

		String[] names = f.getName().split("\\.");
		while (list.size() > 0 && (f = list.element()) != null && 
			   f.getName().startsWith(names[0])) {
			len = f.length();
			f.delete();
			list.remove(f);
			deletedStorage += len;
			Log.d(Config.TAG, f.getPath() + " deleted");
		}

		return deletedStorage;
	}

	public void setShockRunning(boolean shock) {
		shockRunning = shock;
		if (shock)
			++shockHappened;
	}

	public boolean isAllRecording() {
		boolean ret = true;
		for (DataRecorder r : recorders) {
			ret = (ret && r.isRecording());
		}
		return ret;
	}

}
