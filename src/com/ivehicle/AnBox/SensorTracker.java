package com.ivehicle.AnBox;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.ivehicle.AnBox.SensorNotCaptured;
import com.ivehicle.AnBox.SensorSample;
import com.ivehicle.AnBox.R;
import com.ivehicle.util.Log;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
// import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;
// import android.widget.Toast;

//==================================================
// carOrientation: 센서출력값. 화면 표시위해서는 +PI/2
// 기타 모든 각도는 rad.
// 모든 속도는 km/h
//==================================================
public class SensorTracker implements 
	SensorEventListener, LocationListener, GpsStatus.Listener,
	DataStorageManager.DataRecorder {

	private SensorSampleIterator iter = null;
	private SensorSample lowerSample = null;
	private SensorSample middleSample = null;
	private SensorSample upperSample = null;
	private SensorSample returnSample = null;
	private long prevReqTime = 0;

	public interface OnShockEventListener {
		public void onShock(ShockEvent shockEvent); // in milli-seconds
	};

	private Vector<OnShockEventListener> eventListeners = 
		new Vector<OnShockEventListener>();

	Handler handler = new Handler();

	// address
	public String addressString = null;
	// longitude, latitude
	public Location currLoc = new Location(LocationManager.GPS_PROVIDER);

	// velocity, orientation
	public float velGPS = 0;
	private float carSpeed = 0;
	private float carOrientation = 0, theta_p = 0, theta_r = 0;

	// acc
	SensorManager sensorManager;
	private float curAcc[] = new float[3]; // acc w.r.t. local coordinates
	private float prevAcc[] = new float[3];
	private float diffAccGlobal[] = new float[3];
	public float[] accGlobal = new float[3];// = new float[3]; // acc w.r.t. global

	// coordinates
	public float[] prevAccGlobal = new float[3]; // acc w.r.t. global
	// coordinates
	public float[] velGlobal = new float[3]; // velocity (x,y,z) in global
	// coordinates.

	// Orientation
	private float orientation[] = new float[3]; // azimuth, pitch(-180~180),
	// roll(-90~90),
	private float mags[] = new float[3];

	private boolean isReadyAcc = false;
	private boolean isReadyOri = false; // isReadyMags = false, 

	// for calibration
	public int cnt=0;
	private long curT;

	// gps
	public int satellites = 0;
	private boolean towerEnabled = false, gpsEnabled = false;
	private boolean isUsingGps = false;
	LocationManager locationManager = null;
	LocationManager towerLocationManager = null;

	// calibration parameters
	public boolean isAccCalibrating = false, isAccCalibrated = false;
	public float[] accBound = new float[6]; // acc_x:min, max,
	// acc_y:min,max,
	// acc_z:min,max
	public float[] oriBound = new float[2];
	private long lastUpdate = 0, timediff = 0;
	public float[] GravityCalibrated = { -SensorManager.STANDARD_GRAVITY,
			-SensorManager.STANDARD_GRAVITY, -SensorManager.STANDARD_GRAVITY }; // calibrated
	// gravity.
	// (default=-9.8)
	public float[] accStatic = new float[3]; //
	public float[] accCal = new float[3];
	public float thetaPcal=0, thetaRcal = 0;
	public float thetaPStatic=0, thetaRStatic = 0;
	private float sp0,cr0,sr0; //cp0

	private float sp=0,cp=1,sr=1,cr=0;

	// update period
	public int periodGpsUpdate; // GPS update period
	public int minimumDistance; // minimum distance for GPS update
	public int periodSensorRecording; // acc update period
	public long periodAccUpdate;
	public float accThreshold;
	// Config.WriteFileInterval. long WriteFileInterval = 5 * 60 * 1000; // 5분

	private RecordActivity activity;
	RandomAccessFile sensorDataFile = null;

	// Shock
	public TimedDigitalFilter accHistory = new TimedDigitalFilter(Config.SHK_LENGTH);
	public double[] accMean = new double[3];
	public boolean isShockOccurredX = false;
	public boolean isShockOccurredY = false;
	public boolean isShockOccurredZ = false;
	public boolean isShockReleased = true;
	RandomAccessFile accidentDataFile = null;
	private long latestShockTimeX=0,latestShockTimeY=0,latestShockTimeZ=0;
	private boolean recording = false;
	String shockTimeString = null;
	SimpleDateFormat shockTimeFormat = new SimpleDateFormat("HH:mm");
	Date date = new Date(0);
	//MediaPlayer mpAccX, mpAccY, mpAccZ;
	public boolean mpPlayed = true;

	// private Toast toastS, toastE;
	final DecimalFormat df = new DecimalFormat("000");
	final DecimalFormat dfLoc = new DecimalFormat(".000");
	String currentStatus = null;
	public boolean isGpsEnabled = false;
	long wallClockTime;

	// map animation
	final int xBound = 100;
	final int yBound = 100;
	
	MapController mapController;
	public MyPositionOverlay positionOverlay;
	MapView myMapView;
	
	public SensorTracker(RecordActivity act) {
		// initialize
		activity = act;
		addressString = activity.getString(R.string.no_address);
		currentStatus = activity.getString(R.string.starting_sensor);
		shockTimeString = activity.getString(R.string.no_shock);
	}

	public void initMapView() {
		myMapView = (MapView) activity.findViewById(R.id.myMapView);

		mapController = myMapView.getController();
		myMapView.setBuiltInZoomControls(true);

		// Configure the map display options
		myMapView.setSatellite(false);
		myMapView.setStreetView(true);
		//myMapView.setTraffic(true);

		// Zoom in
		mapController.setZoom(17);

		// Add the MyPositionOverlay
		positionOverlay = new MyPositionOverlay();
		List<Overlay> overlays = myMapView.getOverlays();
		try {
			if (positionOverlay != null || overlays != null)
				overlays.add(positionOverlay);
		} catch (Exception ex) {
			//activity.SetStatus("Overlay error: " + ex.getMessage());
		}
	}

	public class TimedDigitalFilter {

		// final int history_len = 4;
		public double[] mLocHistoryX;// = new double[history_len];
		public double[] mLocHistoryY;
		public double[] mLocHistoryZ;
		public long[] Time;
		int mLocPos = 0;
		int startPos = 0;
		int lastPos = 0;
		boolean isFilled = false;

		// ------------------------------------------------------------------------------------------------------------
		public TimedDigitalFilter(int history_len) {
			mLocHistoryX = new double[history_len];
			mLocHistoryY = new double[history_len];
			mLocHistoryZ = new double[history_len];
			Time = new long[history_len];
		}

		long timeDiff() {
			return Time[lastPos] - Time[startPos];
		}

		void clear() {
			mLocPos = startPos = lastPos=0;
			isFilled = false;
			for (int i=0;i<mLocHistoryX.length-1;i++) {
				mLocHistoryX[i]=mLocHistoryY[i]=mLocHistoryZ[i]=0;
				Time[i] = 0;
			}
		}

		void average(long t, float[] d, double[] res) {
			int div=0;
			res[0] = res[1] = res[2] = 0;

			mLocHistoryX[mLocPos] = d[0];
			mLocHistoryY[mLocPos] = d[1];
			mLocHistoryZ[mLocPos] = d[2];
			Time[mLocPos] = t;
			lastPos = mLocPos;

			for (int i=0; i<mLocHistoryX.length; i++) {
				if (Time[i]-Time[startPos]<Config.MAX_SHK_TIME) {
					div++;
					res[0] += Math.abs(mLocHistoryX[i]);
					res[1] += Math.abs(mLocHistoryY[i]);
					res[2] += Math.abs(mLocHistoryZ[i]);
				}
			}

//			if (isFilled)
//				div = mLocHistoryX.length;
//			else
//				div= mLocPos;
			for (int i=0;i<3;i++)
				res[i] /=div;
			
			mLocPos++;
			if (mLocPos > mLocHistoryX.length - 1) {
				mLocPos = 0;
				isFilled = true;
			}
			if (isFilled) {
				startPos++;
				if (startPos > mLocHistoryX.length -1)
					startPos = 0;
			}
		}
	}

	private void fireShockEvent(long occurredAt) {
		if (eventListeners != null) {
			ShockEvent shockEvent = new ShockEvent(occurredAt);
			TextView myLocationText = (TextView) activity.findViewById(R.id.myLocationText);
			shockEvent.locString = myLocationText.getText().toString();
			shockEvent.addr = activity.getAddressString();
			shockEvent.sensorCapturedAt = accHistory.Time.clone();
			shockEvent.mLocHistoryX = accHistory.mLocHistoryX.clone();
			shockEvent.mLocHistoryY = accHistory.mLocHistoryY.clone();
			shockEvent.mLocHistoryZ = accHistory.mLocHistoryZ.clone();
			for (OnShockEventListener listener : eventListeners) {
				listener.onShock(shockEvent);
			}
		}
		//accHistory.clear();
	}

	public void registerOnShockEventListener(OnShockEventListener listener) {
		eventListeners.add(listener);
	}

	public void unregisterOnShockEventListener(
			OnShockEventListener listener) {
		eventListeners.remove(listener);
	}

	public void initialize() {
		for (int i = 0; i < 3; i++) {
			curAcc[i] = prevAcc[i] = diffAccGlobal[i] = 0;
			accGlobal[i] = prevAccGlobal[i] = velGlobal[i] = 0;
			mags[i] = orientation[i] = 0;
			accStatic[i] = 0; accMean[i] = 0;
		}
		for (int i = 0; i < 6; i++) {
			accBound[i] = 0;
		}
		isAccCalibrating = false;
		isAccCalibrated = Config.isCalibrated();
		oriBound[0] = oriBound[1] = 0;

		timediff = -1;
		lastUpdate = 0;// System.currentTimeMillis();//SystemClock.elapsedRealtime();

		// isReadyMags = false;
		isReadyAcc = false;
		isReadyOri = false;
		periodGpsUpdate = Config.getGpsPeriod(); // GPS update period
		minimumDistance = Config.getGpsUpdateDistance(); // minimum distance for GPS update
		periodSensorRecording = Config.getSensorRecordingPeriod(); // sensor file write period
		periodAccUpdate = Config.getAccPeriod();
		accThreshold = Config.getMinimumShockLevel();
	}

	public synchronized String prepareRecording(long dateRestart) {
		Log.d(Config.TAG, toString() + ".prepareRecording()");
		try {
			if (sensorDataFile != null)
				sensorDataFile.close();

			File accFolder = new File(Config.getDataDir());

			if (!accFolder.exists()) {
				Log.e(Config.TAG, "Could not write acc file ");
				activity.SetStatus("Could not write acc to file (no folder). ");
			}

			String sensorFileName = Config.getSensorFileName(dateRestart);
			File accFile = new File(sensorFileName);

			if (!accFile.exists()) {
				accFile.createNewFile();
			}

			sensorDataFile = new RandomAccessFile(accFile, "rw");

			return sensorFileName;
		} 
		catch (IOException e) {
			Log.e(Config.TAG, "Could not write file " + e.getMessage());
			activity.SetStatus("Could not write to file. " + e.getMessage());
		}

		Log.d(Config.TAG, toString() + ".prepareRecording(): Returning");
		return null;
	}

	// start & stop to support life-cycle of an activity
	public synchronized void start() {
		Log.d(Config.TAG, toString() + ".start()");
		initialize();
		if (!Config.isCalibrated()) {
			AccCalibration();
		}
		else {
			isAccCalibrated = true;
			isAccCalibrating = false;
			GravityCalibrated = Config.getCalibratedGravities();
		}
		StartSensorManager();
		startHandler();
		recording = true;
		Log.d(Config.TAG, toString() + ".start(): Returning");
	}

	public synchronized void stop() {
		Log.d(Config.TAG, toString() + ".stop()");
		recording = false;
		stopHandler();
		StopSensorManager();
		Log.d(Config.TAG, toString() + ".stop(): Returning");
	}

	public synchronized boolean isRecording() {
		return recording;
	}

	// implements SensorEventListener
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public void AccCalibration() {
		// long startT = System.currentTimeMillis();
		// Context context = activity.getApplicationContext();
		// toastS = Toast.makeText(context, "Please, don't move, while calibrating sensors",
		//		Toast.LENGTH_LONG);
		final AlertDialog dlg = new AlertDialog.Builder(activity)
			.setTitle(activity.getString(R.string.calibration_title))
			.setMessage(activity.getString(R.string.calibration_start_msg))
			.setPositiveButton(activity.getString(R.string.alert_dialog_ok), 
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
			}

		}).create();
		dlg.show();
		// toastE = Toast.makeText(context, "Calibration Ended",
		// 		Toast.LENGTH_SHORT);
		// toastS.show();

		cnt=0;accCal[0]=accCal[1]=accCal[2]=thetaPcal=thetaRcal=0;
		isAccCalibrating = true;
		isAccCalibrated = false;
		final Handler hdlr = new Handler();
		hdlr.postDelayed(new Runnable() {

			public void run() {
				isAccCalibrating = false;
				isAccCalibrated = true;
				for (int i=0; i<3;i++) {
					accStatic[i] = accCal[i]/cnt;
					accGlobal[i] = prevAccGlobal[i] = velGlobal[i]= 0;
				}
				thetaPStatic = thetaPcal/cnt;
				thetaRStatic = thetaRcal/cnt;
				sp0 = (float) Math.sin(thetaPStatic);
				// cp0 = (float) Math.cos(thetaPStatic * Math.PI / 180); 
				sr0 = (float) Math.sin(thetaRStatic); 
				cr0 = (float) Math.cos(thetaRStatic); 

				GravityCalibrated[1] = accStatic[1]/sp0;
				GravityCalibrated[2] = -accStatic[2]/cr0;
				GravityCalibrated[0] = -accStatic[0]/sr0;

				SharedPreferences pref = 
					PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
				SharedPreferences.Editor prefEditor = pref.edit();
				prefEditor.putFloat("calibrated_gravity_x", GravityCalibrated[0]);
				prefEditor.putFloat("calibrated_gravity_y", GravityCalibrated[1]);
				prefEditor.putFloat("calibrated_gravity_z", GravityCalibrated[2]);
				prefEditor.putBoolean("need_calibration", false);
				prefEditor.commit();

				accHistory.clear();
				isShockOccurredX = isShockOccurredY = isShockOccurredZ = false;
				dlg.setMessage(activity.getString(R.string.calibration_end_msg));
				dlg.show();
				hdlr.postDelayed(new Runnable() {

					public void run() {
						if (dlg.isShowing())
							dlg.dismiss();
					}

				}, 2000);
				// toastE.show();
//				String text = "gx= "+String.valueOf(GravityCalibrated[0])
//							+" \tgy= "+String.valueOf(GravityCalibrated[1])
//							+" \tgz= "+String.valueOf(GravityCalibrated[2]);
//				activity.SetStatus(text);
			}
		}, Config.CAL_TIME);
	}

	public void onSensorChanged(SensorEvent event) {
		curT = event.timestamp;				// in nanoseconds
		if (curT - lastUpdate < Config.getAccPeriod())
			return;
		wallClockTime = System.currentTimeMillis();
		// curT = System.currentTimeMillis();	//SystemClock.elapsedRealtime();//
		float dt;

		// timediff = SystemClock.elapsedRealtime() - lastUpdate;
		// lastUpdate = SystemClock.elapsedRealtime();
		int type = event.sensor.getType();
		// float accXmin,accXmax,accYmin,accYmax,accZmin,accZmax;
		// accXmin=accXmax=accYmin=accYmax=accZmin=accZmax=0;
		// float[] values = new float[3];

		if (type == Sensor.TYPE_ACCELEROMETER) {
			isReadyAcc = true;
			
			timediff = curT - lastUpdate;
			if (timediff>0) {
				prevAcc = curAcc.clone();
				curAcc = event.values.clone();
			}
			if (lastUpdate == 0) {
				// initialize
				timediff = -1;
			}
			lastUpdate = curT;

		} else if (type == Sensor.TYPE_ORIENTATION) {
			isReadyOri = true;
			orientation = event.values.clone();
			// orientation[0] = filter[3].average(orientation[0]);
			orientation[0] *= Config.DEG_TO_RAD;
			theta_p = orientation[1]*Config.DEG_TO_RAD;// = filter[4].average(orientation[1]);
			theta_r = orientation[2]*Config.DEG_TO_RAD;// = filter[5].average(orientation[2]);
			sp = (float) Math.sin(theta_p);
			cp = (float) Math.cos(theta_p); 
			sr = (float) Math.sin(theta_r); 
			cr = (float) Math.cos(theta_r); 
			if (curAcc[2] < 0 && cr > 0)
				cr *= -1;
		} else {
			// isReadyMags = true;
			mags = event.values.clone();
		}

		if (isAccCalibrating && isReadyAcc && isReadyOri) {
			isReadyAcc = isReadyOri = false;
			cnt++;

			for (int i = 0; i < 3; i++) {
				//accStatic[i] = filterAccGlobal[i].average(accGlobal[i]);
				accCal[i] += curAcc[i];
			}
			thetaPcal += theta_p;
			thetaRcal += theta_r;
		}

		if (isAccCalibrated && isReadyAcc && timediff>0) {// isAccCalibrated && isReadyAcc &&  isReadyOri) {
			isReadyAcc =isReadyOri = false;

			// method 2
			if (Math.abs(cp)<1e-3)
				accGlobal[0]=0;
			else
				accGlobal[0] = - (curAcc[1] - GravityCalibrated[1] * sp) / cp;//-curAcc[1]/cp - accStatic[0];//
			if (Math.abs(sr)<.1) {
				accGlobal[1] = 0;
				accGlobal[2] = 0;
			}
			else {
				//if (cr<0) cr = cr*-1;
				accGlobal[1] = -(curAcc[2]+GravityCalibrated[2]*cr)/sr;//-curAcc[2]/sr + accStatic[1];//
				accGlobal[2] = (curAcc[0]/sr + GravityCalibrated[0]);//curAcc[0]*sr - accStatic[2];//
			}
			dt = (float) timediff / 1000000000.0f;
			
			if (timediff <= 0) {
				velGlobal[0] = velGlobal[1] = velGlobal[2] = 0;
				diffAccGlobal[0] = diffAccGlobal[1] = diffAccGlobal[2] = 0;
			} else {
				for (int i = 0; i < 3; i++) {
					diffAccGlobal[i] = (accGlobal[i] - prevAccGlobal[i]) / (dt);					
					velGlobal[i] += 3.6f * ((accGlobal[i] + prevAccGlobal[i]) * dt / 2);
				}
			}

			// Decide whether a shock has occurred
			//accHistory.average(wallClockTime, accGlobal, accMean);
			accHistory.average(curT, accGlobal, accMean);
			if ( !(isShockOccurredX || isShockOccurredY) ) { 
			   if (accMean[0] > accThreshold || accMean[1] > accThreshold) {
					if (accMean[0]>accMean[1]) {
						isShockOccurredX = true;
						latestShockTimeX = curT;
					}
					else{
						isShockOccurredY = true;
						latestShockTimeY = curT;
					}
					fireShockEvent(wallClockTime);
					date.setTime(wallClockTime);
					shockTimeString = shockTimeFormat.format(date);
					mpPlayed = false;
					isShockReleased = false;
				} else if (accMean[2] > Config.getMinimumShockLevel()*.5) {
					isShockOccurredZ = true;
					latestShockTimeZ = curT;
					mpPlayed = false;
					isShockReleased = false;
				}
			}
			prevAccGlobal = accGlobal.clone();
			//carSpeed = velGlobal[1];
			carSpeed = velGPS;
			carOrientation = orientation[0];
//			if (carSpeed <10)
//				currLoc.setBearing(orientation[0]);
		}

		/*
		 * if (mags != null && curAcc != null && isReadyMags) { isReadyMags =
		 * false; SensorManager.getRotationMatrix(mR, I, curAcc, mags);
		 * SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Z,
		 * SensorManager.AXIS_MINUS_X, outR); SensorManager.getOrientation(outR,
		 * oriFromMag); //int[] v = new int[3];
		 * 
		 * //oriFromMag = values.clone(); //oriFromMag[0] =
		 * filter[0].average(values[0] * 100); //oriFromMag[1] =
		 * filter[1].average(values[1] * 100); //oriFromMag[2] =
		 * filter[2].average(values[2] * 100);
		 * 
		 * //Exp.mText01.setText("" + v[0]); //Exp.mText02.setText("" + v[1]);
		 * //Exp.mText03.setText("" + v[2]); }
		 */
	}

	public void StartSensorManager() {
		StartGpsManager();
		
		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(
				this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(
				this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
//		sensorManager.registerListener(
//				this, 
//				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//				SensorManager.SENSOR_DELAY_FASTEST);
		currentStatus = activity.getString(R.string.sensing_started);
	}

	public void StopSensorManager() {
		StopGpsManager();
		sensorManager.unregisterListener(this);
		currentStatus= activity.getString(R.string.sensing_stopped);
		if (sensorDataFile != null) {
			try {
				sensorDataFile.close();
			} catch (IOException e) {
				Log.e(Config.TAG, e.toString());
			}
			sensorDataFile = null;
		}
	}

	public void RestartSensorManagers() {
		stopHandler();
		StopSensorManager();
		StartSensorManager();
		startHandler();
	}

	public void StartGpsManager() {

		locationManager = 
			(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		towerLocationManager = 
			(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		CheckTowerAndGpsStatus();

		if (gpsEnabled) {
			// gps satellite based
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, periodGpsUpdate,
					minimumDistance, this);

			locationManager.addGpsStatusListener(this);

			isUsingGps = true;
		} else if (towerEnabled) {
			isUsingGps = false;
			// Cell tower and wifi based
			towerLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, periodGpsUpdate,
					minimumDistance, this);

		} else {
			isUsingGps = false;
			currentStatus = activity.getString(R.string.no_gps_provider);
			return;
		}

		// SetStatus("Started");
	}

	private void CheckTowerAndGpsStatus() {
		towerEnabled = towerLocationManager.isProviderEnabled(
				LocationManager.NETWORK_PROVIDER);
		gpsEnabled = locationManager.isProviderEnabled(
				LocationManager.GPS_PROVIDER);
	}

	public void StopGpsManager() {
		towerLocationManager.removeUpdates(this);
		locationManager.removeUpdates(this);
		locationManager.removeGpsStatusListener(this);
	}

	public void startHandler() {
		//final TextView txtAccel = (TextView) activity.findViewById(R.id.txtAccel);

		final GraphView speedGraph = (GraphView) activity.findViewById(R.id.SpeedGraph);
		final GraphView accGraph = (GraphView) activity.findViewById(R.id.MapviewAccGraph);				
		// final GraphView accGraphX = (GraphView) activity.findViewById(R.id.MapviewAccGraphX);				
		// final GraphView accGraphZ = (GraphView) activity.findViewById(R.id.MapviewAccGraphZ);				
		speedGraph.SetDataSize(50, 0, 20);
		accGraph.SetDataSize(50, 0, 20);
		// accGraphX.SetDataSize(50, 0, 20);
		// accGraphZ.SetDataSize(50, 0, 20);
		final TextView speedText = (TextView) activity.findViewById(R.id.SpeedText);
		final CompassView compassView = (CompassView)activity.findViewById(R.id.CompassView);
		final TextView Area = (TextView) activity.findViewById(R.id.Area);
		final TextView prevShockText = (TextView) activity.findViewById(R.id.ShockTime);

		handler.postDelayed(new Runnable() {
			public void run() {
				writeSensorToFile();

				//tempDirection = tempDirection+5;
				compassView.RotateCompass((float)getCurrentOrientation()*Config.RAD_TO_DEG);

				speedText.setText(
					df.format(getCurrentSensorValue(SensorSample.VELOCITY))
					+ " km/h");
				Area.setText(addressString);
				prevShockText.setText(
					activity.getString(R.string.shock_at) + " " + shockTimeString);

				//shock = accMean[0]+accMean[1]+accMean[2];
				
				//shock = accMean[0];//Math.abs(getCurrentSensorValue(SensorSample.ACC_Y_VEHICLE));
				accGraph.AppendData((float) accMean[1]);
				speedGraph.AppendData((float) accMean[1]);
				// accGraphX.AppendData((float) accMean[0]);
				// accGraphZ.AppendData((float) accMean[2]);

				if (isShockOccurredX) {
					if (curT - latestShockTimeX > Config.getShockRunningTime() * 1000000L) {
						isShockOccurredX = false;
						currentStatus = activity.getString(R.string.normal);
						isShockReleased = true;
					}
					else {
						currentStatus = activity.getString(R.string.side_shock);
//						if (!mpPlayed) {
//							mpAccX.start();
//							mpPlayed = true;
//						}
					}
				} else if (isShockOccurredY) {
						if (curT - latestShockTimeY > Config.getShockRunningTime() * 1000000L) {
							isShockOccurredY = false;
							currentStatus = activity.getString(R.string.normal);
							isShockReleased = true;
						}
						else {
							currentStatus = activity.getString(R.string.frontal_shock);
//							if (!mpPlayed) {
//								mpAccY.start();
//								mpPlayed = true;
//							}
						}
				} else if (isShockOccurredZ) {
						if (curT - latestShockTimeZ > Config.getShockRunningTime() * 1000000L) {
							isShockOccurredZ = false;
							currentStatus = activity.getString(R.string.normal);
							isShockReleased = true;
						}
						else {
							currentStatus= activity.getString(R.string.speed_bump);
//							if (!mpPlayed) {
//								mpAccZ.start();
//								mpPlayed = true;
//							}
						}
				}
				
				activity.SetStatus(currentStatus);

/*				txtAccel.setText("X: "
					+ df.format(getCurrentSensorValue(SensorSample.ACC_X_VEHICLE))
					+ "\tY: "
					+ df.format(getCurrentSensorValue(SensorSample.ACC_Y_VEHICLE))
					+ "\tZ: "
					+ df.format(getCurrentSensorValue(SensorSample.ACC_Z_VEHICLE))
					+ "\nori:"
					+ df.format(getCurrentSensorValue(SensorSample.ORIENTATION))
					+ "\tpitch:"
					+ String.valueOf(getCurrentSensorValue(SensorSample.THETA_P))
					+ "\troll:"
					+ String.valueOf(getCurrentSensorValue(SensorSample.THETA_R))
					+ "\nvelGPS:\t"
					+ df.format(velGPS) + "km/h"
					+ "\nvelX:"
					+ df.format(getCurrentSensorValue(SensorSample.VEL_X_VEHICLE)) + "km/h"
					+ "\tvelY:"
					+ df.format(getCurrentSensorValue(SensorSample.VEL_Y_VEHICLE)) + "km/h"
					+ "\tvelZ:"
					+ df.format(getCurrentSensorValue(SensorSample.VEL_Z_VEHICLE)) + "km/h"
					+ "\nshock occured:" + String.valueOf(isShockOccurred)
					+ "\nperiod: " + String.valueOf(accHistory.timeDiff())
					+ "\naccMean: "+ df.format(accMean[0])
					+" \t"+ df.format(accMean[1])
					+" \t"+ df.format(accMean[2]));
*/
				handler.postDelayed(this, periodSensorRecording);
			}
		}, periodSensorRecording);
	}

	public void stopHandler() {
		handler.removeCallbacksAndMessages(null);
	}

	public void RestartGpsManagers() {
		StopGpsManager();
		StartGpsManager();
	}

	public void ResetManagersIfRequired() {
		CheckTowerAndGpsStatus();

		// If GPS is enabled
		if (gpsEnabled) {
			// But we're not currently using GPS
			if (!isUsingGps) {
				RestartGpsManagers();
			}
			// Else do nothing
		}

	}

	// implements LocationListener, GpsStatus.Listener
	public void onLocationChanged(Location location) {
		try {
			if (location != null) {
				updateWithNewLocation(location);
				Log.e(Config.TAG,
					"GeneralLocationListener.onLocationChanged(): location updated");
			}
		} catch (Exception ex) {
			currentStatus = ex.getMessage();
			Log.e(Config.TAG, ex.getMessage());
		}
	}

	public void onProviderDisabled(String arg0) {
		updateWithNewLocation(null);
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	public void onGpsStatusChanged(int event) {
		switch (event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			currentStatus = activity.getString(R.string.gps_fix);
			isGpsEnabled = true;
			break;

		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

			GpsStatus status = locationManager.getGpsStatus(null);

			Iterator<GpsSatellite> it = status.getSatellites().iterator();
			int count = 0;
			while (it.hasNext()) {
				count++;
				GpsSatellite oSat = (GpsSatellite) it.next();
				Log.i(Config.TAG,
					"GeneralLocationListener.onGpsStatusChange(): Satellites:"
						+ oSat.getSnr());
			}

			satellites = count; // SetSatelliteInfo(count);
			break;

		case GpsStatus.GPS_EVENT_STARTED:
			currentStatus= activity.getString(R.string.gps_started);
			isGpsEnabled = false;
			break;

		case GpsStatus.GPS_EVENT_STOPPED:
			currentStatus = activity.getString(R.string.gps_stopped);
			isGpsEnabled = false;
			break;

		}

	}

	// For test: 
	// int dcnt=0;
	private void updateWithNewLocation(Location location) {
		//Log.e(Config.TAG, "RecordActivity.updateWithNewLocation()");
		TextView myLocationText = (TextView) activity.findViewById(R.id.myLocationText);

		String latLongString;
		// String addressString = "No address found";

		if (isUsingGps) {

			// WriteToFile(location);
			// WriteToFileLoc(lastUpdate, location);

			// Update the map location.
			// For test: 
			// location.setLatitude(location.getLatitude()+dcnt++*.0005);
			Double geoLat = location.getLatitude() * 1E6;
			Double geoLng = location.getLongitude() * 1E6;
			GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());

			// map animation
			Point pxpoint = new Point();
			Projection pxprojection = myMapView.getProjection();
			pxprojection.toPixels(point, pxpoint);
			if ((pxpoint.x) < xBound
					|| (pxpoint.x) > myMapView.getWidth() - xBound
					|| (pxpoint.y) < yBound
					|| (pxpoint.y) > myMapView.getHeight() - yBound)
				mapController.animateTo(point);

			// Update my location marker
			// activity.positionOverlay.setLocation(location);

			// Calculate speed by GPS
//			float t = location.distanceTo(positionOverlay.location)
//					/ (location.getTime() - positionOverlay.location.getTime())*.0036f; // km/h
//			velGPS = velGlobal[1] = t;
			velGlobal[0] = velGlobal[2] = 0f; // z-axis. temporary.
			//location.setSpeed(t);
//			if (t<10) {
//				carOrientation = orientation[0];
//				positionOverlay.isSetOrientation = true;
//				location.setBearing(carOrientation);
//			}			
//			else
//				positionOverlay.isSetOrientation = false;
			location.setBearing(carOrientation);
			//location.setTime(wallClockTime);
			positionOverlay.setLocation(location);
			currLoc = location;
			velGPS = velGlobal[1] = location.getSpeed();

			latLongString = activity.getString(R.string.latitude) + 
				dfLoc.format(location.getLatitude()) + " " +
				activity.getString(R.string.longitude) + 
				dfLoc.format(location.getLongitude());


		} else {
			latLongString = activity.getString(R.string.no_location);
		}

		myLocationText.setText(latLongString);
	}

	public void writeSensorToFile() {

		try {

			String data = String.valueOf(System.currentTimeMillis()) + " "
					+ String.valueOf(currLoc.getLatitude()) + " "
					+ String.valueOf(currLoc.getLongitude()) + " "
					+ String.valueOf(carSpeed) + " "
					+ String.valueOf(carOrientation) + " "
					+ String.valueOf(theta_p) + " " + String.valueOf(theta_r)
					+ " " + String.valueOf(accGlobal[0]) + " "
					+ String.valueOf(accGlobal[1]) + " "
					+ String.valueOf(accGlobal[2]) + " "
					+ String.valueOf(curAcc[0]) + " "
					+ String.valueOf(curAcc[1]) + " "
					+ String.valueOf(curAcc[2]) + " "
					+ String.valueOf(mags[0]) + " " + String.valueOf(mags[1])
					+ " " + String.valueOf(mags[2]) + " "
					+ String.valueOf(velGlobal[0]) + " "
					+ String.valueOf(velGlobal[1]) + " "
					+ String.valueOf(velGlobal[2]) + " "
					+ String.valueOf(diffAccGlobal[0]) + " "
					+ String.valueOf(diffAccGlobal[1]) + " "
					+ String.valueOf(diffAccGlobal[2]) + " " 
					+ String.valueOf(velGPS) + "\n";

			sensorDataFile.write(data.getBytes());

		} catch (IOException e) {
			Log.e(Config.TAG, "Could not write file " + e.getMessage());
			currentStatus = "Could not write to file. " + e.getMessage();
		}

	}

	// provides clients with current values of various sensors
	public double getCurrentLongitude() {
		return currLoc.getLongitude();
	}

	public double getCurrentLatitude() {
		return currLoc.getLatitude();
	}

	public double getCurrentVelocity() {
		return (double) carSpeed;
	}

	public double getCurrentOrientation() {
		return (double) carOrientation;
	}

	public double getCurrentSensorValue(int sensorType) {

		switch (sensorType) {
		case SensorSample.LATITUDE:
			return currLoc.getLatitude();
		case SensorSample.LONGITUDE:
			return currLoc.getLongitude();
		case SensorSample.VELOCITY:
			return (double) carSpeed;
		case SensorSample.ORIENTATION:
			return (double) carOrientation;
		case SensorSample.THETA_P:
			return (double) theta_p;
		case SensorSample.THETA_R:
			return (double) theta_r;
		case SensorSample.ACC_X_VEHICLE:
			return (double) accGlobal[0];
		case SensorSample.ACC_Y_VEHICLE:
			return (double) accGlobal[1];
		case SensorSample.ACC_Z_VEHICLE:
			return (double) accGlobal[2];
		case SensorSample.ACC_X_PHONE:
			return (double) curAcc[0];
		case SensorSample.ACC_Y_PHONE:
			return (double) curAcc[1];
		case SensorSample.ACC_Z_PHONE:
			return (double) curAcc[2];
		case SensorSample.MAG_X:
			return mags[0];
		case SensorSample.MAG_Y:
			return mags[1];
		case SensorSample.MAG_Z:
			return mags[2];
		case SensorSample.VEL_X_VEHICLE:
			return (double) velGlobal[0];
		case SensorSample.VEL_Y_VEHICLE:
			return (double) velGlobal[1];
		case SensorSample.VEL_Z_VEHICLE:
			return (double) velGlobal[2];
		case SensorSample.DIFFACC_X_VEHICLE:
			return (double) diffAccGlobal[0];
		case SensorSample.DIFFACC_Y_VEHICLE:
			return (double) diffAccGlobal[1];
		case SensorSample.DIFFACC_Z_VEHICLE:
			return (double) diffAccGlobal[2];
		default:
			return SensorSample.WRONG_SENSOR_TYPE;
		}
	}

	public double[] getCurrentAllSensorValues() {
		double[] ret = new double[SensorSample.ARRAY_SIZE];

		for (int i = 0; i < SensorSample.ARRAY_SIZE; i++)
			ret[i] = getCurrentSensorValue(i);
		return ret;
	}

	// hjs
	public String getCurrentAddress() {
		double lat = currLoc.getLatitude();
		double lng = currLoc.getLongitude();

		Geocoder gc = new Geocoder(activity, Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		try {
			List<Address> addresses = gc.getFromLocation(lat, lng, 1);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);

				for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
					sb.append(address.getAddressLine(i)).append("\n");

				sb.append(address.getLocality()).append("\n");
				sb.append(address.getPostalCode()).append("\n");
				sb.append(address.getCountryName());
			}
			addressString = sb.toString();
		} catch (IOException e) {
			currentStatus = e.getMessage();
		}

		return addressString;
	}

	// provides clients with sensor values at a specified time
	public void prepareSensorValueHistorySince(long timeInMs)
		throws SensorNotCaptured {
		iter = new SensorSampleIterator(timeInMs);
		lowerSample = iter.next();
		middleSample = iter.next();
		upperSample = iter.next();
	}

	public void doneWithSensorValueHistory() {
		iter = null;
		lowerSample = null;
		middleSample = null;
		upperSample = null;
	}

	public double getLongitudeAt(long timeInMs) throws SensorNotCaptured {
		return getAllSensorValuesAt(timeInMs)[SensorSample.LONGITUDE];
	}

	public double getLatitudeAt(long timeInMs) throws SensorNotCaptured {
		return getAllSensorValuesAt(timeInMs)[SensorSample.LATITUDE];
	}

	public double getVelocityAt(long timeInMs) throws SensorNotCaptured {
		return getAllSensorValuesAt(timeInMs)[SensorSample.VELOCITY];
	}

	public double getOrientationAt(long timeInMs) throws SensorNotCaptured {
		return getAllSensorValuesAt(timeInMs)[SensorSample.ORIENTATION];
	}

	public double getSensorValueAt(long timeInMs, int sensorType)
		throws SensorNotCaptured {
		return getAllSensorValuesAt(timeInMs)[sensorType];
	}

	public double[] getAllSensorValuesAt(long timeInMs) 
		throws SensorNotCaptured {
		return getSensorSampleAt(timeInMs).values;
	}

	public SensorSample getSensorSampleAt(long timeInMs)
		throws SensorNotCaptured {
		// little optimization for getXXXAt(), removing lb & ub computation and
		// condition check
		if (prevReqTime == timeInMs) {
			return returnSample;
		}
		else {
			prevReqTime = timeInMs;
		}

		long lb = (lowerSample.sampledAt + middleSample.sampledAt) / 2;
		long ub = (middleSample.sampledAt + upperSample.sampledAt) / 2;

		if (lb <= timeInMs && timeInMs < ub) {
			// time is between lb and ub
			returnSample = middleSample;
			return returnSample;
		}
		else if (ub <= timeInMs) {
			// time is greater than or equal to ub
			while (iter.hasNext()) {
				lowerSample = middleSample;
				middleSample = upperSample;
				upperSample = iter.next();
				lb = (lowerSample.sampledAt + middleSample.sampledAt) / 2;
				ub = (middleSample.sampledAt + upperSample.sampledAt) / 2;

				if (lb <= timeInMs && timeInMs < ub) {
					returnSample = middleSample;
					return returnSample;
				}
			}
			// boundary condition check
			if (upperSample.sampledAt + 10 * 1000 <= timeInMs) {
				throw new SensorNotCaptured(timeInMs);
			}
			else {
				returnSample = upperSample;
				return returnSample;
			}
		}
		else {
			// time is less than lb. bound condition check
			if (timeInMs < lowerSample.sampledAt - 10 * 1000) {
				throw new SensorNotCaptured(timeInMs);
			}
			else {
				returnSample = lowerSample;
				return returnSample;
			}
		}
	}

}
