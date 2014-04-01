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

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.EditText;
import android.provider.Contacts.People;

public class SettingActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.setting);
	}
	
	private EditText m_numberEditText = null;
	public void SetPhoneNumberEditText(EditText numberEditText)
	{
		m_numberEditText = numberEditText;
	}
	

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String[] projection = new String[] { People.NUMBER };
		ContentResolver cr = getContentResolver();

		// This case means click back at Contact-Activity
		if(data == null)
			return;
		
		Cursor contactCursor = cr.query(data.getData(), projection, null, null,
				null);

		
		String PhoneNumber;
		try {
			contactCursor.moveToFirst();
			PhoneNumber = contactCursor.getString(0);
		} finally {
			// contactCursor.close();
		}

		if(m_numberEditText != null)
			m_numberEditText.setText(PhoneNumber);
	}
}
