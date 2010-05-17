package com.ivehicle.AnBox;

import com.ivehicle.util.Log;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.*;

import java.util.ArrayList;

public class GraphView extends View {
	private final String m_className = "GraphView";
	private Paint textPaint;
	private Paint graphPaint;
	private Paint backgroundPaint;
	private ArrayList<Float> m_dataSet;
	private int m_tempIndex = 0;
	private float zeroValue;
	private float rangeValue;
	private float maxValue;

	private void InitGraphView() {
		Log.i(m_className, "GraphView() is called.");

		textPaint = new Paint(/* Paint.ANTI_ALIAS_FLAG */);
		textPaint.setColor(Color.argb(0x60, 0xff, 0xff, 0xff));
		textPaint.setTextSize(20);

		graphPaint = new Paint(/* Paint.ANTI_ALIAS_FLAG */);
		graphPaint.setColor(Color.argb(0x60, 0xff, 0x00, 0x00));
		// graphPaint.setStyle(Paint.Style.STROKE);

		backgroundPaint = new Paint(/* Paint.ANTI_ALIAS_FLAG */);
		backgroundPaint.setColor(Color.argb(0x60, 0x00, 0x00, 0x00));
		// graphPaint.setStyle(Paint.Style.STROKE);

		m_dataSet = new ArrayList<Float>();
	}

	public GraphView(Context context) {
		super(context);
		InitGraphView();
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		InitGraphView();
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		InitGraphView();
	}

	public void SetDataSize(long dataSize, float min, float max) {
		m_dataSet.clear();
		for (long i = 0; i < dataSize; i++)
			m_dataSet.add(new Float(0.0));
		m_tempIndex = 0;
		rangeValue = max - min;
		maxValue = max;
	}

	public void AppendData(float data) {
		m_dataSet.set(m_tempIndex % m_dataSet.size(), new Float(data));
		m_tempIndex = (m_tempIndex + 1) % m_dataSet.size();
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(m_className, "onDraw() is called.");

		int width = getWidth();
		int height = getHeight();

		canvas.drawRect(0, 0, width, height, backgroundPaint);

		int dataNum = m_dataSet.size();
		zeroValue = height / 2f;
		for (int i = 0; i < m_dataSet.size(); i++) {
			float value;
			value = m_dataSet.get((m_tempIndex + dataNum-1-i) % dataNum)
					.floatValue();

			//path.lineTo(width*i/dataNum, height/rangeValue*(maxValue-value));
			canvas.drawRect(width * i / dataNum, height / rangeValue
					* (maxValue - value), width * (i + 1) / dataNum, height,
					graphPaint);
		}
		canvas.drawLine(0, zeroValue, width, zeroValue, textPaint);
	}

}
