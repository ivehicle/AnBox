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

import java.io.File;
import java.io.IOException;

import com.ivehicle.AnBox.R;
import com.ivehicle.util.Log;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VisionRecorder implements 
	SurfaceHolder.Callback, DataStorageManager.DataRecorder,
	MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

	private static final int ST_NOT_INIT = 0;
	private static final int ST_RECORDER_INIT = 1;
	private static final int ST_PREVIEWING = 2;
	private static final int ST_RECORDING = 3;
	private static final int ST_WAITING_SF = 4;
	private static final int ST_SURFACE_INIT = 5;

	private int state = ST_NOT_INIT;
	private long dateRestart = 0;

	private Activity act_ = null;
	PowerManager.WakeLock wl = null;
	private String videoFileName = null;
	private ContentValues videoValues = null;
	private MediaRecorder recorder = null;
	private SurfaceHolder surfaceHolder = null;
	private long recordingStarted = 0;

	public VisionRecorder(Activity act, boolean faked) {
		act_ = act;
		SurfaceView sv = (SurfaceView)act_.findViewById(R.id.surface_view);
		SurfaceHolder surfaceHolder = sv.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		Log.d(Config.TAG, toString() + "(): after init SurfaceHolder");
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		Log.d(Config.TAG, "VisionRecorder.onError(): what = " + what + " extra = " + extra);
	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		Log.d(Config.TAG, "VisionRecorder.onInfo(): what = " + what + " extra = " + extra);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(Config.TAG, "Surface created!");
		surfaceHolder = holder;
		handleSurfaceCreatedEvent();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(Config.TAG, "Surface changed!");
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(Config.TAG, "Surface destroyed!");
		handleStopEvent();
		surfaceHolder = null;
		if (state == ST_SURFACE_INIT)
			state = ST_NOT_INIT;
	}

	private void createVideoPath(long dateTaken) {
		Log.d(Config.TAG, toString() + ".createVideoPath()");
		String title = Config.getFileName(dateTaken);
		String filename = Config.getMovieFileName(dateTaken);
		ContentValues values = new ContentValues(8);
		values.put(Video.Media.TITLE, title);
		values.put(Video.Media.DISPLAY_NAME, title + "." + Config.MOVIE_EXT);
		values.put(Video.Media.DATE_TAKEN, dateTaken);
		values.put(Video.Media.MIME_TYPE, Config.MOVIE_MIME_TYPE);
		values.put(Video.Media.DATA, filename);
		videoFileName = filename;
		Log.v(Config.TAG, toString() + 
			".createVideoPath(): Current camera video filename: " +
			videoFileName);
		videoValues = values;
	}

	private void initRecording() {
		createVideoPath(dateRestart);

		if (wl == null) {
			PowerManager pm = (PowerManager) act_.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, Config.TAG);
			wl.acquire();
		}

		if (recorder == null) {
			recorder = new MediaRecorder();
		}
		recorder.reset();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setOutputFile(videoFileName);
		recorder.setVideoFrameRate(15);
		recorder.setVideoSize(640, 480);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

		Log.d(Config.TAG, toString() + ".initRecording(): after init MediaRecorder, Returning");
	}

	private void startPreview() throws IllegalStateException, IOException {
		recorder.setPreviewDisplay(surfaceHolder.getSurface());
		recorder.prepare();
		Log.d(Config.TAG, toString() + ".startPreview(): after MediaRecorder.prepare()");
	}

	private void startRecording() {
		recorder.start();
		recordingStarted = System.currentTimeMillis();
		Log.d(Config.TAG, toString() + ".startRecording(): after MediaRecorder.start()");
	}

	private void stopRecording() {
		Log.d(Config.TAG, toString() + ".stopRecording(): stop recording...");
		recorder.stop();
		Log.d(Config.TAG, toString() + ".stopRecording(): Recorded video filename:" + videoFileName);

		videoValues.put(Video.Media.DURATION, System.currentTimeMillis()
				- recordingStarted);
		videoValues.put(Video.Media.SIZE, new File(videoFileName).length());
		Uri videoUri = act_.getContentResolver().insert(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoValues);
		if (videoUri == null) {
			Log.d(Config.TAG, toString() + ".stopRecording(): Content resolver failed");
			return;
		}
		Log.d(Config.TAG, toString() + ".stopRecording(): Video URI = " + videoUri.getPath());
		videoValues = null;

		// Force Media scanner to refresh now. Technically, this is
		// unnecessary, as the media scanner will run periodically but
		// helpful for testing.
		act_.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				videoUri));
		Log.d(Config.TAG, toString() + ".stopRecording(): Video file published");
	}

	private void deinitRecording() {
		recorder.reset();
		recorder.release();
		recorder = null;
		wl.release();
		wl = null;
	}

	private void handlePrepareEvent() {
		try {
			switch (state) {
			case ST_NOT_INIT:
				initRecording();
				state = ST_RECORDER_INIT;
				break;
	
			case ST_RECORDING:
				stopRecording();
				initRecording();
				startPreview();
				startRecording();
				state = ST_RECORDING;
				break;

			case ST_SURFACE_INIT:
				initRecording();
				startPreview();
				state = ST_PREVIEWING;
				break;

			default:
				break;
			}
		}
		catch (Exception e) {
			handleExceptionEvent(e);
		}
	}

	private void handleSurfaceCreatedEvent() {
		try {
			switch (state) {
			case ST_NOT_INIT:
				initRecording();
				startPreview();
				state = ST_PREVIEWING;
				break;

			case ST_RECORDER_INIT:
				startPreview();
				state = ST_PREVIEWING;
				break;

			case ST_WAITING_SF:
				startPreview();
				startRecording();
				state = ST_RECORDING;
				break;

			default:
				break;
			}
		}
		catch (Exception e) {
			handleExceptionEvent(e);
		}
	}

	private void handleStartEvent() {
		try {
			switch (state) {
			case ST_NOT_INIT:
				initRecording();
				state = ST_WAITING_SF;
				break;

			case ST_RECORDER_INIT:
				state = ST_WAITING_SF;
				break;

			case ST_PREVIEWING:
				startRecording();
				state = ST_RECORDING;
				break;

			case ST_SURFACE_INIT:
				initRecording();
				startPreview();
				startRecording();
				state = ST_RECORDING;
				break;

			default:
				break;
			}
		}
		catch (Exception e) {
			handleExceptionEvent(e);
		}
	}

	private void handleStopEvent() {
		try {
			switch (state) {
			case ST_RECORDER_INIT:
			case ST_WAITING_SF:
				deinitRecording();
				state = ST_NOT_INIT;
				break;

			case ST_PREVIEWING:
				deinitRecording();
				state = ST_SURFACE_INIT;
				break;

			case ST_RECORDING:
				stopRecording();
				deinitRecording();
				state = ST_SURFACE_INIT;
				break;
	
			default:
				state = ST_NOT_INIT;
				break;
			}
		}
		catch (Exception e) {
			handleExceptionEvent(e);
		}
	}

	private void handleExceptionEvent(Exception e) {
		Log.e(Config.TAG, "Exception occurred: ", e);
		deinitRecording();
		switch (state) {
		case ST_NOT_INIT:
		case ST_RECORDER_INIT:
		case ST_WAITING_SF:
			state = ST_NOT_INIT;
			break;
			
		case ST_PREVIEWING:
		case ST_RECORDING:
		case ST_SURFACE_INIT:
			state = ST_SURFACE_INIT;
			break;
		}
	}

	public synchronized String prepareRecording(long dateRestart) {
		Log.d(Config.TAG, toString() + ".prepareRecording()");
		this.dateRestart = dateRestart;
		handlePrepareEvent();
		Log.d(Config.TAG, toString() + ".prepareRecording(): Returning");
		return videoFileName;
	}

	public synchronized void start() {
		Log.d(Config.TAG, toString() + ".start()");
		handleStartEvent();
	}

	public synchronized void stop() {
		handleStopEvent();
	}

	public synchronized boolean isRecording() {
		return (state == ST_RECORDING);
	}

}
