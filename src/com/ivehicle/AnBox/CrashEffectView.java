package com.ivehicle.AnBox;

import com.ivehicle.util.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class CrashEffectView extends View {

	public CrashEffectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_handler = new Handler();
		m_runnable = new CrashEffectViewRunnable();
	}

	private Paint textPaint;
	private Paint effectPaint1;
	private Paint effectPaint2;
	private Paint effectPaint3;
	private Paint effectPaint4;
	private int edgesize = 50;
	private int clampsize = 2;
	private int grandentsize = edgesize - clampsize;
	private final int endState = 41;
	
	private Handler m_handler;
	private CrashEffectViewRunnable m_runnable;

	class CrashEffectViewRunnable implements Runnable
	{
		private final String m_className = "TimerRunnable";

		public void run()
		{
			Log.i(m_className, "run() is called.");			
			CrashEffectRunnable();			
		}
	}
	
	private int m_effectFSM = endState;
	private boolean m_isdisplayEffect = false;
	
	private void CrashEffectRunnable()
	{
		if(m_effectFSM %2 == 0)
			m_isdisplayEffect = true;
		else 
			m_isdisplayEffect = false;
		
		if(m_effectFSM >= endState)
		{
			invalidate();
			m_isdisplayEffect = false;
			return;
		}

		invalidate();
		m_handler.postDelayed(m_runnable, 500);
		m_effectFSM++;
	
	}

	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

		int width = right-left;
		int textSize = width/10;
		
		textPaint = new Paint();
		textPaint.setColor(Color.rgb(255, 0, 0));
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);

		effectPaint1 = new Paint();
		effectPaint1.setShader(new LinearGradient(0, 0, 0, grandentsize, Color
				.argb(255, 255, 0, 0), Color.argb(0, 255, 0, 0),
				Shader.TileMode.CLAMP));
		effectPaint2 = new Paint();
		effectPaint2.setShader(new LinearGradient(0, bottom - grandentsize, 0,
				bottom, Color.argb(0, 255, 0, 0), Color.argb(255, 255, 0, 0),
				Shader.TileMode.CLAMP));
		effectPaint3 = new Paint();
		effectPaint3.setShader(new LinearGradient(0, 0, grandentsize, 0, Color
				.argb(255, 255, 0, 0), Color.argb(0, 255, 0, 0),
				Shader.TileMode.CLAMP));
		effectPaint4 = new Paint();
		effectPaint4.setShader(new LinearGradient(right - grandentsize, 0,
				right, 0,
				Color.argb(0, 255, 0, 0),
				Color.argb(255, 255, 0, 0),
				Shader.TileMode.CLAMP));
	}

	protected void onDraw(Canvas canvas) {
		if(m_isdisplayEffect == false)
			return;
		
		int width = getWidth();
		int height = getHeight();
		canvas.drawRect(0, 0, width, grandentsize, effectPaint1);
		canvas.drawRect(0, height - grandentsize, width, height, effectPaint2);
		canvas.drawRect(0, 0, grandentsize, height, effectPaint3);
		canvas.drawRect(width-grandentsize, 0, width, height, effectPaint4);	
		canvas.drawText("Shock Detected!", width/2, height/2, textPaint);
	}
	
	public void ShockEvent()
	{
		if(m_effectFSM >= endState)
			m_handler.postDelayed(m_runnable, 500);
		m_effectFSM = 0;
	}
}
