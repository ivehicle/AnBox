package com.ivehicle.AnBox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

	private Paint circlePaint1;
	private Paint circlePaint2;
	private Paint linePaint;
	private Bitmap compassBitmap;
	private Bitmap rotatedBitmap;
	
	private int width = 48;
	private int height = 48;


	private boolean isInitialized = false;
	protected synchronized void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		
		/*
		width = right-left;
		height = bottom-top;
		compassBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.compass);
		compassBitmap = Bitmap.createScaledBitmap(compassBitmap, width, height, false);

		isInitialized = true;
		*/
	}
	
	private void Init() {
		/*
		linePaint = new Paint();
		linePaint.setColor(Color.rgb(240, 240, 240));

		circlePaint1 = new Paint();
		circlePaint1.setColor(Color.rgb(0, 0, 0));

		circlePaint2 = new Paint();
		circlePaint2.setColor(Color.argb(80, 80, 80, 80));
		*/
	}

	public synchronized void RotateCompass(float degrees) {
		/*
		if (isInitialized == false)
			return;
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.preRotate(-degrees+90, 0, 0);
		rotatedBitmap = Bitmap.createBitmap(compassBitmap, 0, 0, width, height,
				rotateMatrix, false);
		*/
	}

	public CompassView(Context context) {
		super(context);
		Init();
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init();
	}

	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init();
	}

	protected synchronized void onDraw(Canvas canvas) {
		/*
		if (isInitialized == false)
			return;

		int length = width / 2;

		// canvas.drawCircle(width / 2, height / 2, width/2, circlePaint1);
		canvas.drawCircle(width / 2, height / 2, width / 2, circlePaint2);
		canvas.drawLine(0, height / 2, width - 1, height / 2, linePaint);
		canvas.drawLine(width / 2, 0, width / 2, height - 1, linePaint);

		canvas.drawLine(
				width/2+(float)Math.cos(Math.PI*1/4)*length,
				height/2+(float)Math.sin(Math.PI*1/4)*length,
				width/2+(float)Math.cos(Math.PI*5/4)*length,
				height/2+(float)Math.sin(Math.PI*5/4)*length, linePaint);
		canvas.drawLine(
				width/2+(float)Math.cos(Math.PI*3/4)*length,
				height/2+(float)Math.sin(Math.PI*3/4)*length,
				width/2+(float)Math.cos(Math.PI*7/4)*length,
				height/2+(float)Math.sin(Math.PI*7/4)*length, linePaint);

		// 48x48
		canvas.drawBitmap(rotatedBitmap, width / 2
				- (rotatedBitmap.getWidth() + 1) / 2, height / 2
				- (rotatedBitmap.getHeight() + 1) / 2, null);
		*/
	}
}
