package com.ivehicle.AnBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ivehicle.util.Log;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.format.DateFormat;

public class RecordingServer extends Service {

	public static final String TAG = "RECSRV";
	public static final String RECORDING_SERVICE = 
		"com.ivehicle.AnBox.RecordingServer.SERVICE";
	public static final String IPADDR = "127.0.0.1";
	public static final int PORT = 12345;
	
	private ServerSocket sock = null;
	private String videoFileName = null;
	private ContentValues videoValues = null;
	private long recordingStarted = 0;

	private static final int NOT_MATCHING = 0;
	private static final int PAT_MATCHING = 1;
	private static final int PAT_MATCHED = 2;
	
	private static final byte[] ftypePat = 
		{ 0x00, 0x00, 0x00, 0x1c, 0x66, 0x74, 0x79, 0x70 };
	// private static final byte[] moviePat = 
	//	{ 0x6d, 0x6f, 0x6f, 0x76 };

    private static String createName(long dateTaken) {
    	return DateFormat.format(Config.FILE_NAME_FORMAT, dateTaken).toString();
    }

    private void createVideoPath() {
    	recordingStarted = System.currentTimeMillis();
    	String title = createName(recordingStarted);
    	String displayName = title + ".3gp";
    	File cameraDir = new File(Config.getDataDir());
    	cameraDir.mkdirs();
    	SimpleDateFormat dateFormat = new SimpleDateFormat(Config.FILE_NAME_FORMAT);
    	Date date = new Date(recordingStarted);
    	String filepart = dateFormat.format(date);
    	String filename = Config.getDataDir() + "/" + filepart + ".3gp";
    	ContentValues values = new ContentValues(8);
    	values.put(Video.Media.TITLE, title);
    	values.put(Video.Media.DISPLAY_NAME, displayName);
    	values.put(Video.Media.DATE_TAKEN, recordingStarted);
    	values.put(Video.Media.MIME_TYPE, "video/3gpp");
    	values.put(Video.Media.DATA, filename);
    	videoFileName = filename;
    	Log.v(TAG, "Current camera video filename: " + videoFileName);
    	videoValues = values;
	}
    
	private class DataHandler implements Runnable {

		public void run() {
			while (true) {
				Socket acceptedSock = null;
				try {
					acceptedSock = sock.accept();
				}
				catch (Exception e) {
					Log.e(TAG, e.toString());
				}
				
				createVideoPath();
				storeData(acceptedSock);
			}
		}
		
		private int searchPattern(byte[] buf, int startPos, int len, byte[] pat) {
			int posBuf = startPos;
			int posPat = 0;
			int start = 0;
			int state = NOT_MATCHING;
			
			while (posBuf < len - 1) {
				if (buf[posBuf] == pat[posPat]) {
					++posPat;
					switch (state) {
					case NOT_MATCHING:
						start = posBuf;
						state = PAT_MATCHING;
						break;
						
					case PAT_MATCHING:
						if (posPat == pat.length)
							state = PAT_MATCHED;
						break;
					}
				}
				else {
					posPat = 0;
					start = 0;
					state = NOT_MATCHING;
				}
				
				if (state == PAT_MATCHED) {
					return start;
				}
				++posBuf;
			}
			return -1;
		}

		private byte[] ftypeHeader = {
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		};

		private void rememberFtypeHeader(byte[] buf, int pos) {
			int cnt = 0;
			while (cnt < 28) {
				ftypeHeader[cnt] = buf[pos + cnt];
				++cnt;
			}
			ftypeHeader[cnt++] = buf[pos-4];
			ftypeHeader[cnt++] = buf[pos-3];
			ftypeHeader[cnt++] = buf[pos-2];
			ftypeHeader[cnt] = buf[pos-1];
		}

		private void storeData(Socket s) {
			File store = null;
			FileOutputStream fos = null;
			InputStream is = null;

			try {
				store = new File(videoFileName);
				fos = new FileOutputStream(store);
				is = s.getInputStream();

				byte[] buf = new byte[4096];
				int len = 0;
				while ((len = is.read(buf)) != -1) {
					int ftypeHeaderOffset = -1;
					Log.v(TAG, "data received = " + len + " bytes");
					if ((ftypeHeaderOffset = searchPattern(buf, 0, len, ftypePat)) >= 0) {
						Log.d(TAG, "ftypePat found at pos " + ftypeHeaderOffset);
						rememberFtypeHeader(buf, ftypeHeaderOffset);
					}

					if (ftypeHeaderOffset >= 0) {
						fos.write(buf, 0, ftypeHeaderOffset - 4);
						fos.write(buf, ftypeHeaderOffset + 28, len - ftypeHeaderOffset - 28);
					}
					else {
						fos.write(buf, 0, len);
					}
					fos.flush();
					Log.v(TAG, "data written");
				}
				fos.flush();
				fos.close();
				s.close();

				Log.v(TAG, "constructing header info...");
				RandomAccessFile raf = new RandomAccessFile(store, "rw");
				raf.seek(0);
				raf.write(ftypeHeader);
				Log.d(TAG, "wrote ftype header = " + ftypeHeader.toString());
				raf.close();
				raf = null;
				
		        videoValues.put(Video.Media.DURATION, 
		        		System.currentTimeMillis() - recordingStarted);
		        videoValues.put(Video.Media.SIZE, new File(videoFileName).length());
		        Uri videoUri = getContentResolver().insert(
		        		MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
		        		videoValues
		        		);
		        if (videoUri == null) {
		            Log.d(TAG, "Content resolver failed");
		            return;
		        }
		        Log.d(TAG, "Video URI = " + videoUri.getPath());
		        videoValues = null;

		        // Force Media scanner to refresh now. Technically, this is
		        // unnecessary, as the media scanner will run periodically but
		        // helpful for testing.
		        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			finally {
				if (fos != null)
					fos = null;
				if (store != null)
					store = null;
				if (s != null)
					s = null;
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			sock = new ServerSocket(PORT);
		}
		catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		new Thread(new DataHandler()).start();
	}
	
	@Override
	public void onDestroy() {
		try {
			sock.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		super.onDestroy();
	}
}
