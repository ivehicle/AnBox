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

import java.util.StringTokenizer;

import com.ivehicle.AnBox.R;
import com.ivehicle.util.Log;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
// import android.telephony.gsm.SmsManager;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SOSMessageSender implements 
	SensorTracker.OnShockEventListener {

	private class SMSTimer extends EasyTimer {

		private int mProgress = 0;
		private ShockEvent event;

		private int maxProgress;
		private ProgressDialog dlg = null;
		private Context context = null;

		public SMSTimer(ProgressDialog dlg, Context context, int period, int maxProgress, ShockEvent event) {
			super(period);
			this.dlg = dlg;
			this.event = event;
			this.maxProgress = maxProgress;
			this.context = context;
		}

		protected void doRun() {
			if(dlg.isShowing() == false) {
				Stop();						
				return;
			}

			if (mProgress >= maxProgress) {
				// Progress Dialog Stop
				Stop();
				dlg.dismiss();

				// Send SMS
				String callNumber = Config.getPhoneNumberList();
				String sendMessage = Config.getMessageToSend();
				
				if (callNumber == null || sendMessage == null ||
					callNumber == "" || sendMessage == "")
					return;

				if (event.addr != null)
					sendMessage = sendMessage.replaceAll("\\[addr\\]", event.addr);
				if (event.locString != null)
					sendMessage = sendMessage.replaceAll("\\[gps\\]", event.locString);
				if (sendMessage.length() >= 60)
					sendMessage = sendMessage.substring(0, 59);

				// Toast Dialog show
				Toast toast = Toast.makeText(context,
					parentAct.getString(R.string.sending_sms) + " " + sendMessage, Toast.LENGTH_LONG);
				toast.show();

				StringTokenizer tokenizer = new StringTokenizer(callNumber, "\n");
				while (tokenizer.hasMoreElements())
				{
					String num = tokenizer.nextToken();
	        		SmsManager sms = SmsManager.getDefault();
	        		try {
	        			sms.sendTextMessage(num, null, sendMessage, null, null);
	        		}
	        		catch (Exception e) {
	        			Log.e(Config.TAG, "Exception occurred while sending SMS " + e.toString());
	        			Log.i(Config.TAG, "message = " + sendMessage);
	        		}
				}
			}
			else {
				++mProgress;
				dlg.setProgress(mProgress);
			}
		}
	}

	private Activity parentAct;
	private EasyTimer mTimer;
	private long prevShockTime = 0;

	public SOSMessageSender(Activity act) {
		this.parentAct = act;
	}

	public void onShock(ShockEvent shockEvent) {
		if (System.currentTimeMillis() - prevShockTime < Config.getShockRunningTime()) {
			Log.i(Config.TAG, "SOSMessageSender: Ignoring consecutively recurring shock events");
			return;
		}

		prevShockTime = shockEvent.occurredAt;

		if (!Config.getSendSOSMsg()) {
			Log.i(Config.TAG, "SOSMessageSender: Send SOS Message flag is not set");
			Log.i(Config.TAG, "SOSMessageSender: The shock event is ignored");
			return;
		}

		ProgressDialog mProgressDialog = new ProgressDialog(parentAct);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setProgress(0);
		mProgressDialog.setMax(Config.getSOSMsgCancelWaitTime());
		mProgressDialog.setTitle(parentAct.getString(R.string.sos_dialog_title));
		mProgressDialog.setButton(
			parentAct.getString(R.string.alert_dialog_cancel),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
		mProgressDialog.show();

		mTimer = new SMSTimer(
			mProgressDialog, parentAct.getApplicationContext(),
			1000, Config.getSOSMsgCancelWaitTime(), shockEvent);
		mTimer.Start();		
	}

}
