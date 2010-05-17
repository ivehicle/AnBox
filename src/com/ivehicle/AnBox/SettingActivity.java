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