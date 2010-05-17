package com.ivehicle.AnBox;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.ivehicle.AnBox.R;
import com.ivehicle.util.Log;

import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
// import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.MapActivity;

public class RecordActivity extends MapActivity {

	public final static int RESULT_RESTART = RESULT_FIRST_USER + 1;

	private VisionRecorder recorder = null;
	private SensorTracker tracker = null;
	private ShockEventRecorder shkRec = null;
	private DataStorageManager dsm = null;
	private SOSMessageSender sosMsgSender = null;
	private ShockEffect shockEffect = null;
	private BroadcastReceiver receiver = null;
	// private Button btnReset;
	private boolean recording = false;
	private boolean justCreated = true;

	//address thread
	AddressThread aThread;
	private Handler mMainHandler, mChildHandler;
	String address=null;
	Bundle latLong;
	private double[] latLongArray;
	private boolean isReceivedMsg=false, isSendMsg=false;
	private String addressSMS = null;

	//play alert sound
	Handler paMainHandler;
	MediaPlayer mpAccX;
	MediaPlayer mpAccY;
	MediaPlayer mpAccZ;

	private class ActivityFSM {
		public static final int SplashScreenActivity = 0;
		public static final int RecordActivity = 1;
		public static final int MapViewActivity = 2;
	}
	
	private int m_fsm = ActivityFSM.SplashScreenActivity;
	private Handler hdlr = new Handler();

	private void ChangeActivityEffect(int fsm)
	{
		switch(fsm)
		{
		case ActivityFSM.SplashScreenActivity:
			findViewById(R.id.RecordLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.RecordInfoLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.MapViewLayout).setVisibility(View.INVISIBLE);
			findViewById(R.id.StatusLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.SplashScreenLayout).setVisibility(View.VISIBLE);
			hdlr.postDelayed(new Runnable() {
				public void run() {
					if (!recording) {
						dsm.start();
						recording = true;
						justCreated = false;
					}
					ChangeActivityEffect(ActivityFSM.RecordActivity);
					hdlr.removeCallbacksAndMessages(null);
				}
			}, 10000);
			break;
		case ActivityFSM.RecordActivity:
			findViewById(R.id.RecordLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.RecordInfoLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.MapViewLayout).setVisibility(View.INVISIBLE);
			findViewById(R.id.StatusLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.SplashScreenLayout).setVisibility(View.INVISIBLE);
			if (!recording) {
				dsm.start();
				recording = true;
				justCreated = false;
			}
			break;
		case ActivityFSM.MapViewActivity:
			findViewById(R.id.RecordLayout).setVisibility(View.INVISIBLE);
			findViewById(R.id.RecordInfoLayout).setVisibility(View.INVISIBLE);
			findViewById(R.id.MapViewLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.StatusLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.SplashScreenLayout).setVisibility(View.INVISIBLE);
			if (!recording) {
				dsm.start();
				recording = true;
				justCreated = false;
			}
			break;
		}
		m_fsm = fsm;
	}

	private void ButtonAnimation() {
		final ImageButton leftScrollButton = (ImageButton) findViewById(R.id.LeftScrollButton);
		final ImageButton rightScrollButton = (ImageButton) findViewById(R.id.RightScrollButton);

		AnimationSet set = new AnimationSet(true);
		Animation aAni = new AlphaAnimation(1.0f, 0.0f);
		aAni.setDuration(3000);
		aAni.setStartOffset(3000);
		aAni.setFillAfter(true);
		set.setFillAfter(true);
		leftScrollButton.startAnimation(aAni);		
		rightScrollButton.startAnimation(aAni);		
	}

	class AddressThread extends Thread {
    	
		Context ctx;
		Geocoder gc;// = new Geocoder(this, Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		List<Address> addresses;
		double[] latlong;
		Address address;
		String addressString=null;
		int cnt=0;
		public AddressThread(Context context) {
			ctx = context;
			gc = new Geocoder(ctx, Locale.getDefault());
			latlong = new double[2];
			mChildHandler = new Handler() {
    			
    			public void handleMessage(Message msg) {
    			
    				//SetStatus("child->main");
    				Log.d("addr", toString() + ".handleMessage(): Got an incoming message from the main thread");
    				latlong = msg.getData().getDoubleArray("latlong");    				

    				try {
    					addresses = gc.getFromLocation(latlong[0], latlong[1], 1);
    					Log.d("addr", toString() + ".handleMessage(): after Geocoder.getFromLocation()");
    					if (addresses.size() > 0) {
    						address = addresses.get(0);

    						//for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
    						//	sb.append(address.getAddressLine(i)).append("\n");

    						sb.append(address.getFeatureName()).append("\n");
    						sb.append(address.getThoroughfare()).append("\n");    						
    						sb.append(address.getLocality());//.append("\n");
//    						sb.append(address.getLocale()).append("\n");
//    						sb.append(address.getAdminArea()).append("\n");
//    						sb.append(address.getSubAdminArea()).append("\n");
    						addressSMS = address.getLocality() + " " + address.getThoroughfare()
    								   + " " + address.getFeatureName();
    					}
    					addressString = sb.toString();
    					sb.delete(0,sb.length());
    				} catch (IOException e) {
    					Log.w("addr", "Exception occurred during Geocoder.getFromLocation()");
    					addressString = RecordActivity.this.getString(R.string.no_address);
    				}

					Message toMain = mMainHandler.obtainMessage();
					toMain.obj = addressString;
					mMainHandler.sendMessage(toMain);

					Log.d("addr", toString() + ".handleMessage(): Send a message to the main thread");
    			}
    		};
		}
		
    	public void run() {
    		// SetStatus("child run");
    		this.setName("child");
    		
    		/*
			 * You have to prepare the looper before creating the handler.
			 */
			Looper.prepare();
			
			/*
			 * Create the child handler on the child thread so it is bound to the
			 * child thread's message queue.
			 */

    		
    		Log.d(Config.TAG, "Child handler is bound to - " + mChildHandler.getLooper().getThread().getName());

    		Looper.loop();
    	}
    }

	void startSettingActivity()
	{
		startActivity(new Intent(this, SettingActivity.class));
	}

	void startRecordViewerActivity()
	{
		startActivity(new Intent(this, RecordViewerActivity.class));
	}

	private void smoothScrollTo(int pos) {
		FrameLayout recordLayout = (FrameLayout)findViewById(R.id.RecordLayout);
		FrameLayout mapLayout = (FrameLayout)findViewById(R.id.MapViewLayout);
		final long animationDuration = 1000;
		
		switch(pos) {
			case 0:
			{
		        Animation animation;
		        animation = new TranslateAnimation(
		                Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		            );
		        animation.setDuration(animationDuration);
		        recordLayout.startAnimation(animation);	        
				recordLayout.setVisibility(View.VISIBLE);
				
		        animation = new TranslateAnimation(
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 1.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		            );
		        animation.setDuration(animationDuration);
		        mapLayout.startAnimation(animation);
				mapLayout.setVisibility(View.INVISIBLE);
			}
				break;
			case 1:
		        Animation animation;
		        animation = new TranslateAnimation(
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, -1.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		            );
		        animation.setDuration(animationDuration);
		        recordLayout.startAnimation(animation);	        
				recordLayout.setVisibility(View.INVISIBLE);
				
		        animation = new TranslateAnimation(
		                Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		            );
		        animation.setDuration(animationDuration);
		        mapLayout.startAnimation(animation);
				mapLayout.setVisibility(View.VISIBLE);
				break;
		}
		
		return;
	}

	public void onCreate(Bundle savedInstanceState) {
		Log.d(Config.TAG, toString() + ".onCreate()");
		super.onCreate(savedInstanceState);
		Log.d(Config.TAG, toString() + ".onCreate(): after super.onCreate()");
		setContentView(R.layout.main);
		
		ChangeActivityEffect(ActivityFSM.SplashScreenActivity);
		Button recordBtn = (Button) findViewById(R.id.restart_record);
		recordBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ChangeActivityEffect(ActivityFSM.RecordActivity);
			}
		});
		Button settingBtn = (Button) findViewById(R.id.restart_setting);
		settingBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startSettingActivity();
			}
		});
		Button viewBtn = (Button) findViewById(R.id.restart_view);
		viewBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startRecordViewerActivity();
			}
		});
		Button exitBtn = (Button) findViewById(R.id.restart_exit);
		exitBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

		final ImageButton leftScrollButton = (ImageButton) findViewById(R.id.LeftScrollButton);
		final ImageButton rightScrollButton = (ImageButton) findViewById(R.id.RightScrollButton);
		// Default Position
		leftScrollButton.setEnabled(false);
		rightScrollButton.setEnabled(true);
		ButtonAnimation();
		
		// Button Scroll Setting
		leftScrollButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				leftScrollButton.setEnabled(false);
				rightScrollButton.setEnabled(true);
				ButtonAnimation();
				smoothScrollTo(0);
				ChangeActivityEffect(ActivityFSM.RecordActivity);

				tracker.positionOverlay.isVisible=false;
			}
		});
		rightScrollButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				leftScrollButton.setEnabled(true);
				rightScrollButton.setEnabled(false);
				ButtonAnimation();
				smoothScrollTo(1);
				ChangeActivityEffect(ActivityFSM.MapViewActivity);

				tracker.positionOverlay.isVisible=true;
			}
		});

		// Button Animation at touch screen
		View.OnTouchListener btnAniListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					ButtonAnimation();
				return false;
			}
		};		

		RelativeLayout mainFrameLayout;
		mainFrameLayout = (RelativeLayout)findViewById(R.id.RecordInfoLayout);
		mainFrameLayout.setOnTouchListener(btnAniListener);	
		mainFrameLayout = (RelativeLayout)findViewById(R.id.Relative02);
		mainFrameLayout.setOnTouchListener(btnAniListener);

		Log.d(Config.TAG, toString() + ".onCreate(): after built UI. external storage state = " + 
			Environment.getExternalStorageState());

		if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0) {
			dsm = new DataStorageManager();
		}
		Log.d(Config.TAG, toString() + ".onCreate(): after creating DSM");
		// If you want to disable video recording, then set faked to true
		boolean isFaked = false;
		recorder = new VisionRecorder(this, isFaked);
		tracker = new SensorTracker(this);
		shkRec = new ShockEventRecorder();
		sosMsgSender = new SOSMessageSender(this);
		shockEffect = new ShockEffect(this);

		// Init RecorderView
		//final GraphView speedGraph = (GraphView) findViewById(R.id.SpeedGraph);
		//speedGraph.SetDataSize(100, 0, 40);
		/*
		btnReset = (Button) this.findViewById(R.id.buttonReset);
		btnReset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tracker.AccCalibration();
			}
		});
		*/

		tracker.initMapView();
		Log.d(Config.TAG, toString() + ".onCreate(): Returning");
	}

	protected void LayoutChange(final View oldView, final View newView,
			int direction) {
		/*
		 * final int duration = 500; newView.setVisibility(View.VISIBLE);
		 * AnimationSet oldViewAniSet = new AnimationSet(true); AnimationSet
		 * newViewAniSet = new AnimationSet(true); // Animation effect1
		 * Animation ani = new AlphaAnimation(1.0f, 0.0f);
		 * ani.setDuration(duration); oldViewAniSet.addAnimation(ani); // ani =
		 * new AlphaAnimation(0.0f, 1.0f); // ani.setDuration(duration); //
		 * newViewAniSet.addAnimation(ani);
		 * 
		 * // Animation effect2 ani = new TranslateAnimation(
		 * Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
		 * -direction, Animation.RELATIVE_TO_SELF, 0.0f,
		 * Animation.RELATIVE_TO_SELF, 0.0f ); ani.setDuration(duration);
		 * oldViewAniSet.addAnimation(ani); ani = new TranslateAnimation(
		 * Animation.RELATIVE_TO_SELF, direction, Animation.RELATIVE_TO_SELF,
		 * 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
		 * 0.0f ); ani.setDuration(duration); newViewAniSet.addAnimation(ani);
		 * // Start Animation newView.startAnimation(newViewAniSet);
		 * oldView.startAnimation(oldViewAniSet); // Set invisible after
		 * animation(duration) Handler handler = oldView.getHandler();//new
		 * Handler(); handler.postDelayed(new Runnable() { public void run() {
		 * if(newView.getVisibility() == View.VISIBLE)
		 * oldView.setVisibility(View.INVISIBLE); } }, duration);
		 */
	}

	public void SetStatus(String message) {
		TextView tvStatus = (TextView) findViewById(R.id.textStatus);
		tvStatus.setText(message);
	}

	private class ShockNotifierToDSM implements SensorTracker.OnShockEventListener {

		private long prevShockTime = 0;

		public void onShock(ShockEvent shockEvent) {
			// A shock will run for Config.getShockRunningTime() at least
			if (System.currentTimeMillis() - prevShockTime < Config.getShockRunningTime())
				return;

			prevShockTime = shockEvent.occurredAt;
			dsm.setShockRunning(true);
			
			Handler hdlr = new Handler();
			hdlr.postDelayed(new Runnable() {

				public void run() {
					dsm.setShockRunning(false);
				}
				
			}, Config.getShockRunningTime());
		}
		
	}

	private class ShockEffect implements SensorTracker.OnShockEventListener {

		private RecordActivity m_mainActivity = null;
		private long prevShockTime = 0;
		
		public ShockEffect(RecordActivity mainActivity)
		{
			m_mainActivity = mainActivity;
		}
		
		public void onShock(ShockEvent shockEvent) {
			if (System.currentTimeMillis() - prevShockTime < Config.getShockRunningTime()) {
				return;
			}
			prevShockTime = System.currentTimeMillis();
			CrashEffectView view = (CrashEffectView)m_mainActivity.findViewById(R.id.CrashEffectView);
	        view.ShockEvent();
		}
	}

	private void readAddress() {
		// read address
		latLong = new Bundle();
		latLongArray = new double[2];

        mMainHandler = new Handler() {
        	
        	public void handleMessage(Message msg) {
        		//SetStatus("main: message from child");
        		Log.d("addr", "Got an incoming message from the child thread - "  + (String)msg.obj);
        		
        		/*
        		 * Handle the message coming from the child thread.
        		 */
        		tracker.addressString = (String)msg.obj;
        		isReceivedMsg=true;
        	}
        };	
        
        aThread = new AddressThread(getApplicationContext());
        aThread.start();
			
		mMainHandler.postDelayed(new Runnable() {
			public void run() {
				//SetStatus("main->child");
				if (mChildHandler != null) {// && tracker.isGpsEnabled) {
					if (!isSendMsg || isReceivedMsg) {
						Message msg = mChildHandler.obtainMessage();
						latLongArray[0] = tracker.currLoc.getLatitude();
						latLongArray[1] = tracker.currLoc.getLongitude();
						latLong.putDoubleArray("latlong", latLongArray);
						msg.setData(latLong);
						mChildHandler.sendMessage(msg);
						isReceivedMsg=false;
						isSendMsg = true;
					}
					Log.d("addr", "Send a message to the child thread");
					mMainHandler.postDelayed(this,Config.getAddressUpdatePeriod());
				}
			}
		}, Config.getAddressUpdatePeriod());

	}

	private void playAlertSound() {
		mpAccX = MediaPlayer.create(getApplicationContext(), R.raw.kr2_shock_side);
		mpAccY = MediaPlayer.create(getApplicationContext(), R.raw.kr2_shock_front);
		mpAccZ = MediaPlayer.create(getApplicationContext(), R.raw.kr2_shock_vertical);

        paMainHandler = new Handler();			
		paMainHandler.postDelayed(new Runnable() {
			public void run() {
				if ( (tracker.isShockOccurredX || tracker.isShockOccurredY || tracker.isShockOccurredZ)
					&& !tracker.mpPlayed && !tracker.isShockReleased) {
					if (tracker.isShockOccurredX) {
						mpAccX.start();
					}
					else if (tracker.isShockOccurredY) {
						mpAccY.start();
					}
					else {
						mpAccZ.start();
					}
					tracker.mpPlayed = true;
				}
				paMainHandler.postDelayed(this,1000);
			}
		}, 1000);

	}

	private void stopAlertSound() {
		if (paMainHandler != null)
			paMainHandler.removeCallbacksAndMessages(null);
	}

	private ShockNotifierToDSM shkNotif = null;

	private void registerAll() {
		receiver = new BatteryBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(receiver, filter);
		dsm.registerDataRecorder(recorder);
		dsm.registerDataRecorder(tracker);
		dsm.registerDataRecorder(shkRec);
		tracker.registerOnShockEventListener(shkRec);
		shkNotif = new ShockNotifierToDSM();
		tracker.registerOnShockEventListener(shkNotif);
		tracker.registerOnShockEventListener(sosMsgSender);
		tracker.registerOnShockEventListener(shockEffect);
	}

	private synchronized void startAll() {
		registerAll();
		readAddress();
		playAlertSound();
		dsm.prepare();
		if (!justCreated) {
			dsm.start();
			justCreated = false;
		}
	}

	private void unregisterAll() {
		tracker.unregisterOnShockEventListener(shockEffect);
		tracker.unregisterOnShockEventListener(sosMsgSender);
		tracker.unregisterOnShockEventListener(shkNotif);
		shkNotif = null;
		tracker.unregisterOnShockEventListener(shkRec);
		dsm.unregisterDataRecorder(shkRec);
		dsm.unregisterDataRecorder(tracker);
		dsm.unregisterDataRecorder(recorder);
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	private synchronized void stopAll() {
		if (recording) {
			dsm.stop();
			recording = false;
		}
		stopAlertSound();
		if (mChildHandler != null)
			mChildHandler.removeCallbacksAndMessages(null);
		if (mMainHandler != null)
			mMainHandler.removeCallbacksAndMessages(null);
		hdlr.removeCallbacksAndMessages(null);
		unregisterAll();
	}

	public class BatteryBroadcastReceiver extends BroadcastReceiver {

		boolean highTempRestarted = false;

		public BatteryBroadcastReceiver() {
			SharedPreferences pref = 
				PreferenceManager.getDefaultSharedPreferences(RecordActivity.this);
			highTempRestarted = pref.getBoolean("high_temp_restart", false);
		}

		/*
		private void setHighTempRestart(boolean value) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(RecordActivity.this);
			pref.edit().putBoolean("high_temp_restart", value).commit();
		}

		private void restartMainActivity() {
			setHighTempRestart(true);
			// if (recording) {
			//	dsm.stop();
			//	recording = false;
			//	dsm.prepare();
			//	ChangeActivityEffect(ActivityFSM.SplashScreenActivity);
			// }
		}
		*/

		public void onReceive(Context context, Intent intent) {
			Log.w(Config.TAG, toString() + 
				".onReceive(): Action = " + intent.getAction());

			int batStatus = 0;
			if (intent.hasExtra("status")) {
				batStatus = intent.getIntExtra("status", 0);
				Log.w(Config.TAG, toString() + 
					".onReceive(): Battery status = " + batStatus);
			}

			if (intent.hasExtra("temperature")) {
				Log.w(Config.TAG, toString() + 
					".onReceive(): Battery temperature = " + 
					intent.getIntExtra("temperature", 0));
				/*
				if (intent.getIntExtra("temperature", 0) >= 460) {
					if (!highTempRestarted && batStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
						Log.w(Config.TAG, toString() + 
							".onReceive(): Restarting main activity due to high temp");
						restartMainActivity();
					}
					else {
						Log.w(Config.TAG, toString() + 
							".onReceive(): Already restarted main activity");
					}
				}
				else if (highTempRestarted) {
					// Restarted due to high temperature but now temp is lower than 460
					// then, reset the high_temp_restart flag
					Log.w(Config.TAG, toString() + 
						".onReceive(): Resetting high temp restarted");
					setHighTempRestart(false);
					highTempRestarted = false;
				}
				*/
			}

			if (intent.hasExtra("plugged")) {
				Log.w(Config.TAG, "Battery plugged type = " + intent.getIntExtra("plugged", 0));
			}
			if (intent.hasExtra("health")) {
				Log.w(Config.TAG, "Battery health = " + intent.getIntExtra("health", 0));
				/*
				int health = intent.getIntExtra("health", 0);
				if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
					restartMainActivity();
				}
				*/
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(Config.TAG, toString() + ".onResume()");
		super.onResume();

		if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) != 0) {
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.external_storage_error_title))
				.setMessage(getString(R.string.external_storage_error_msg))
				.setNegativeButton(getString(R.string.external_storage_error_btn), 
					new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						RecordActivity.this.setResult(RESULT_OK);
						RecordActivity.this.finish();
					}
				
				}).create().show();
			return;
		}
		Log.d(Config.TAG, toString() + ".onResume(): after checking storage status");

		SharedPreferences sharedPref = 
			PreferenceManager.getDefaultSharedPreferences(this);

		String prefString = sharedPref.getString("IsFirst", "true");
		if(prefString == "true")
		{
			TextView tv = (TextView)findViewById(R.id.restart_text);
			tv.setText(R.string.first_time_exec_msg);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putString("IsFirst", "false");
			prefEditor.commit();
		}
		Log.d(Config.TAG, toString() + ".onResume(): after checking first exec");

		Config.initialize(getApplicationContext());
		Log.d(Config.TAG, toString() + ".onResume(): after Config.initialize()");
		startAll();
	}

	@Override
	protected void onPause() {
		Log.d(Config.TAG, toString() + ".onPause()");
		if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) != 0) {
			super.onPause();
			return;
		}
		stopAll();
		super.onPause();
	}

	public void onBackPressed ()
	{
		switch(m_fsm){
		case ActivityFSM.SplashScreenActivity:
			finish();
//			super.onBackPressed();
			break;
		case ActivityFSM.RecordActivity:
		case ActivityFSM.MapViewActivity:
			ChangeActivityEffect(ActivityFSM.SplashScreenActivity);
			break;
		}
	}

	/*
	@Override
	protected void onStop() {
		Log.d(Config.TAG, toString() + ".onStop()");
		if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) != 0) {
			super.onStop();
			return;
		}
		// stopAll();
		super.onStop();
		if (restartUnderProgress) {
			Log.w(Config.TAG, toString() + 
				".onStop(): Restart is under progress. finishing main activity");
		}
		else {
			Log.d(Config.TAG, toString() + 
				".onStop(): Restart is not under progress.");
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(Config.TAG, toString() + ".onDestroy()");
		if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) != 0) {
			super.onDestroy();
			return;
		}
		// stopAll();
		if (restartUnderProgress) {
			Log.w(Config.TAG, toString() + 
				".onDestroy(): Restart is under progress. finishing main activity");
			setResult(RESULT_RESTART);
		}
		else {
			Log.d(Config.TAG, toString() + 
				".onDestroy(): Restart is not under progress.");
			setResult(RESULT_OK);
		}
		// recorder = null;
		// tracker = null;
		// dsm = null;
		// Intent recsrv = new Intent(
		//	"com.ivehicle.idrive.RecordingServer.SERVICE");
		// stopService(recsrv);
		// mChildHandler.getLooper().quit();
		// mChildHandler=null;
		// mMainHandler=null;
		super.onDestroy();
	}
	*/

	protected boolean isRouteDisplayed() {
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		menu.add(0, 1, Menu.NONE, getString(R.string.menu_view));
		menu.add(0, 2, Menu.NONE, getString(R.string.menu_setting));
		menu.add(0, 3, Menu.NONE, getString(R.string.menu_cal));
		menu.add(0, 4, Menu.NONE, getString(R.string.menu_about));
		// menu.add(0, 5, Menu.NONE, getString(R.string.menu_restart));
		// menu.add(0, 6, Menu.NONE, getString(R.string.menu_exit));
		// menu.add(0, 7, Menu.NONE, getString(R.string.menu_test));
		// menu.add(0, 8, Menu.NONE, getString(R.string.menu_sms));

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		// MapController control = myMapView.getController(); // 맵 컨트롤러를 받아옵니다.

		// Context context = getApplicationContext(); // Toast를 띄우기 위해 Context를
		// 받아옵니다.

		switch (item.getItemId()) {

		case 1:
			startRecordViewerActivity();
			break;

		// Configuration
		case 2:
			startSettingActivity();
			break;

		case 3:
			tracker.AccCalibration();
			break;

		// About
		case 4:
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.about_title))
				.setMessage(
					getString(R.string.about_msg) + " " +
					Config.getVendorName() + ", " +
					Config.getModelName())
				.setPositiveButton(
					getString(R.string.alert_dialog_ok), 
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create().show();
			break;

		/*
		// Restart
		case 5:
			if (recording) {
				dsm.stop();
				dsm.prepare();
				dsm.start();
			}
			break;

		// Exit
		case 6:
			Log.d(Config.TAG, toString() + ".onOptionsItemSelected(): Exit requested");
			Toast toast = Toast.makeText(this.getApplicationContext(),
				getString(R.string.terminating), Toast.LENGTH_LONG);
			toast.show();
			setResult(RESULT_OK);
			finish();
			return false;

		// Test
		case 7:
	        CrashEffectView view = (CrashEffectView)findViewById(R.id.CrashEffectView);
	        view.ShockEvent();
	        break;

		// Send Emergency Message
		case 8:
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			final int MAX_PROGRESS = Integer.parseInt(sharedPref.getString("message_cancel_wait_time", "10"))*1000;
			final ProgressDialog mProgressDialog = new ProgressDialog(this);
			
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(MAX_PROGRESS);

//			mProgressDialog.setIcon(R.drawable.icon);
			mProgressDialog.setTitle("Emergency-message will be sent.");

			mProgressDialog.setButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton)
						{
						}
					});

			mProgressDialog.show();

			final Context context = getApplicationContext();
			final EasyTimer m_timer = new EasyTimer(100) {
				private int mProgress = 0;

				protected void doRun() {
					if(mProgressDialog.isShowing() == false)
					{
						Stop();						
						return;
					}
					
					if (mProgress >= MAX_PROGRESS)
					{
						// Progress Dialog Stop
						Stop();
						mProgressDialog.dismiss();

						// Toast Dialog show
						Toast toast = Toast.makeText(context,
								"Sending Context", Toast.LENGTH_SHORT);
						toast.show();

						// Send SMS
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
						String callNumber = sharedPref.getString("number_of_message_to_send", "");
						String sendMessage = sharedPref.getString("message_to_send", "");
						StringTokenizer tokenizer = new StringTokenizer(callNumber, "\n");


						while (tokenizer.hasMoreElements())
						{
							String num = tokenizer.nextToken();
			        		SmsManager sms = SmsManager.getDefault();
			                sms.sendTextMessage(num, null, sendMessage, null, null); 							
						}

					} else
					{
						mProgress+=100;
						mProgressDialog.setProgress(mProgress);
					}
				}
			};
			m_timer.Start();
		}
			break;
			*/
		}

		return true;
	}

	/*
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			moveTaskToBack(true);
			Toast.makeText(
				getBaseContext(),
				"iDrive is still running. You can exit the application from the menu options.",
				Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	*/

	public String getAddressString() {
		return addressSMS;			// 최근에 읽어들인 주소입니다.
	}
}
