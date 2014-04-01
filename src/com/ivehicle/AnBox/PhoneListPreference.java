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

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneListPreference extends DialogPreference {

	private View m_contentView = null;

	public PhoneListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.phone_list_preference);
	}

	public PhoneListPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.phone_list_preference);
	}

	protected View onCreateDialogView() {
		m_contentView = super.onCreateDialogView();

		String rawString = getPersistedString("\n\n\n");
		StringTokenizer tokenizer = new StringTokenizer(rawString, "\n");

		EditText editText;
		editText = (EditText) m_contentView
				.findViewById(R.id.EmergencyCallNum1);
		if (tokenizer.hasMoreElements())
			editText.setText(tokenizer.nextToken());
		else
			editText.setText("");
		editText = (EditText) m_contentView
				.findViewById(R.id.EmergencyCallNum2);
		if (tokenizer.hasMoreElements())
			editText.setText(tokenizer.nextToken());
		else
			editText.setText("");
		editText = (EditText) m_contentView
				.findViewById(R.id.EmergencyCallNum3);
		if (tokenizer.hasMoreElements())
			editText.setText(tokenizer.nextToken());
		else
			editText.setText("");
		editText = (EditText) m_contentView
				.findViewById(R.id.EmergencyCallNum4);
		if (tokenizer.hasMoreElements())
			editText.setText(tokenizer.nextToken());
		else
			editText.setText("");

		final SettingActivity activity = (SettingActivity) getContext();

		Button button;
		button = (Button) m_contentView.findViewById(R.id.ContactButton1);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Toast Dialog show
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setData(Uri.parse("content://contacts/phones"));
				activity.startActivityForResult(i, 0);
				activity.SetPhoneNumberEditText((EditText) m_contentView
						.findViewById(R.id.EmergencyCallNum1));
			}
		});
		button = (Button) m_contentView.findViewById(R.id.ContactButton2);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Toast Dialog show
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setData(Uri.parse("content://contacts/phones"));
				activity.startActivityForResult(i, 0);
				activity.SetPhoneNumberEditText((EditText) m_contentView
						.findViewById(R.id.EmergencyCallNum2));
			}
		});
		button = (Button) m_contentView.findViewById(R.id.ContactButton3);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Toast Dialog show
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setData(Uri.parse("content://contacts/phones"));
				activity.startActivityForResult(i, 0);
				activity.SetPhoneNumberEditText((EditText) m_contentView
						.findViewById(R.id.EmergencyCallNum3));
			}
		});
		button = (Button) m_contentView.findViewById(R.id.ContactButton4);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Toast Dialog show
				Intent i = new Intent(Intent.ACTION_PICK);
				i.setData(Uri.parse("content://contacts/phones"));
				activity.startActivityForResult(i, 0);
				activity.SetPhoneNumberEditText((EditText) m_contentView
						.findViewById(R.id.EmergencyCallNum4));
			}
		});

		return m_contentView;
	}

	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
	}

	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			EditText editText1 = (EditText) m_contentView
					.findViewById(R.id.EmergencyCallNum1);
			EditText editText2 = (EditText) m_contentView
					.findViewById(R.id.EmergencyCallNum2);
			EditText editText3 = (EditText) m_contentView
					.findViewById(R.id.EmergencyCallNum3);
			EditText editText4 = (EditText) m_contentView
					.findViewById(R.id.EmergencyCallNum4);
			String rawString = editText1.getText().toString() + "\n"
					+ editText2.getText().toString() + "\n"
					+ editText3.getText().toString() + "\n"
					+ editText4.getText().toString();
			persistString(rawString);
		}
		super.onDialogClosed(positiveResult);
	}
}
