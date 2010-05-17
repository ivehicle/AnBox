package com.ivehicle.AnBox;

import com.ivehicle.AnBox.R;
import com.ivehicle.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RestartActivity extends Activity {

	public final static int JUST_START = 0;

	private boolean immediateRestart = false;
	private Handler starter = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		Log.v(Config.TAG, toString() + ".onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restart);
		Button recordBtn = (Button) findViewById(R.id.restart_record);
		recordBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startRecordActivity();
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
	}

	private void startRecordActivity() {
		startActivityForResult(
			new Intent(this, RecordActivity.class),
				JUST_START);
	}

	private void startSettingActivity() {
		startActivity(new Intent(this, SettingActivity.class));
	}

	private void startRecordViewerActivity() {
		startActivity(new Intent(this, RecordViewerActivity.class));
	}

	protected void onResume() {
		Log.v(Config.TAG, toString() + ".onResume()");
		super.onResume();
		if (immediateRestart) {
			Log.v(Config.TAG, toString() + ".onResume(): " +
				"RecordActivity need to be immediately restarted. starting RecordActivity...");
			immediateRestart = false;
			startRecordActivity();
		}
		else {
			Log.v(Config.TAG, toString() + ".onResume(): " +
				"RecordActivity need to exit");
			starter.postDelayed(new Runnable() {
				public void run() {
					startRecordActivity();
				}
			}, 10000);
		}
	}

	protected void onActivityResult(
		int requestCode,int resultCode, Intent data) {

		Log.v(Config.TAG, toString() + ".onActivityResult()");
		if (requestCode == JUST_START) {
            switch (resultCode) {
            case RESULT_CANCELED:
            case RESULT_OK:
        		Log.v(Config.TAG, toString() + ".onActivityResult(): " +
        			"result = canceled or ok");
        		immediateRestart = false;
            	break;

            case RecordActivity.RESULT_RESTART:
        		Log.v(Config.TAG, toString() + ".onActivityResult(): " +
        			"result = restart");
        		immediateRestart = true;
            	break;
            	
            }
        }
    }

	protected void onPause() {
		Log.v(Config.TAG, toString() + ".onPause()");
		starter.removeCallbacksAndMessages(null);
		super.onPause();
	}
}
