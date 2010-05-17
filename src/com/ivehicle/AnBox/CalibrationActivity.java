package com.ivehicle.AnBox;

import com.ivehicle.AnBox.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.SystemClock;

public class CalibrationActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calibration);

		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setProgress(0);
		mProgressDialog.setMax(Config.CAL_TIME);
		mProgressDialog.setTitle("Calibrating...");

		mProgressDialog.show();

		final EasyTimer m_timer = new EasyTimer(100) {
			private int mProgress = 0;

			protected void doRun() {
				if(mProgressDialog.isShowing() == false)
				{
					Stop();						
					return;
				}

				if (mProgress >= Config.CAL_TIME)
				{
					// Progress Dialog Stop
					mProgressDialog.setTitle("Calibrating...done");
					Stop();
					SystemClock.sleep(500);
					mProgressDialog.dismiss();
					SystemClock.sleep(500);
					finish();
				} else
				{
					mProgress += 100;
					mProgressDialog.setProgress(mProgress);
				}
			}
		};
		m_timer.Start();

	}

}
