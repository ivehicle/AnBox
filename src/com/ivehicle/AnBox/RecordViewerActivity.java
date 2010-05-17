package com.ivehicle.AnBox;

import java.util.Collections;

import com.ivehicle.AnBox.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

public class RecordViewerActivity extends Activity {
	
	private String currentlyPlaying = null;
	VideoView videoHolder = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recordviewer);

		final ShockEventList list = new ShockEventList();
		Collections.sort(list, new ShockEvent.ReverseComparator());
		final ListView shockListView = (ListView) findViewById(R.id.AccidentList);
		shockListView.setAdapter(new ArrayAdapter<ShockEvent>(this,
				android.R.layout.simple_list_item_1, list));
		shockListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				videoHolder = (VideoView) findViewById(R.id.RecordView);
				if (currentlyPlaying.compareTo(list.elementAt(position).containingFile) != 0) {
					videoHolder.stopPlayback();
					String movieFilePath = list.elementAt(position).getMovieFilePath();
					videoHolder.setVideoURI(Uri.parse("file://" + movieFilePath));
					videoHolder.requestFocus();
				}
				int startTime = (int)(list.elementAt(position).occurredAt - 10 * 1000 - 
						Config.getTimeFromFileName(list.elementAt(position).containingFile));
				if (startTime < 0)
					startTime = 0;
				videoHolder.seekTo(startTime);
				videoHolder.start();
				currentlyPlaying = list.elementAt(position).containingFile;
			}
		});
		final ListView timeIndex = (ListView) findViewById(R.id.TimeList);

		// Button to return RecordActivity 
		ImageButton closeButton = (ImageButton) findViewById(R.id.CloseButton);
		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				while (videoHolder.isPlaying()) {
					videoHolder.stopPlayback();
					SystemClock.sleep(100);
				}
				finish();
			}
		});

		// Implement about Tab
		final int EnableTextColor = Color.rgb(0x00, 0x00, 0x00);
		final int EnableBkColor = Color.rgb(0xee, 0xee, 0xee);
		final int DisableTextColor = Color.rgb(0x60, 0x60, 0x60);
		final int DisableBkColor = Color.rgb(0x20, 0x20, 0x20);
		
		final TextView textView1 = (TextView) findViewById(R.id.ListTitleText);
		final TextView textView2 = (TextView) findViewById(R.id.ListTitleText2);
		textView1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				textView1.setTextColor(EnableTextColor);
				textView1.setBackgroundColor(EnableBkColor);
				textView2.setTextColor(DisableTextColor);
				textView2.setBackgroundColor(DisableBkColor);
				timeIndex.setVisibility(View.VISIBLE);
				shockListView.setVisibility(View.INVISIBLE);
			}
		});
		textView2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				textView2.setTextColor(EnableTextColor);
				textView2.setBackgroundColor(EnableBkColor);
				textView1.setTextColor(DisableTextColor);
				textView1.setBackgroundColor(DisableBkColor);
				shockListView.setVisibility(View.VISIBLE);
				timeIndex.setVisibility(View.INVISIBLE);
			}
		});
		

		// Movie Viewer
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		videoHolder = (VideoView) findViewById(R.id.RecordView);
		// Media Controller(if you touch screen, you can use.)
		videoHolder.setMediaController(new MediaController(this));
		if (!list.isEmpty()) {
			videoHolder.setVideoURI(Uri.parse("file://" + list.elementAt(0).getMovieFilePath()));
			videoHolder.requestFocus();
	
			int startTime = (int)(list.elementAt(0).occurredAt - 10 * 1000 - 
				Config.getTimeFromFileName(list.elementAt(0).containingFile));
			if (startTime < 0)
				startTime = 0;
			videoHolder.seekTo(startTime);
			videoHolder.start();
			currentlyPlaying = list.elementAt(0).containingFile;
		}

	}
	
	protected void onPause() {
		while (videoHolder.isPlaying()) {
			videoHolder.stopPlayback();
			SystemClock.sleep(100);
		}
		super.onPause();
	}
	
	protected void onStop() {
		finish();
		super.onStop();
	}
}