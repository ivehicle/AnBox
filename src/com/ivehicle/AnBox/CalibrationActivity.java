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
