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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RecordingIcon extends View {

	private Paint lightPaint;
	private Paint iconPaint;
	private Paint greenPaint;
	private Paint grayPaint;
	private Paint redPaint;
	private float m_radius;

	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int width = right-left;// getWidth();
		int height = bottom-top;// getHeight();
		m_radius = (float) (width < height ? width / 2.0 : height / 2.0);

		lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		lightPaint.setShader(new LinearGradient(width / 2, 0, width / 2,
				(m_radius - 4 - 4) * 2, Color.argb(255, 255, 255, 255), Color
						.argb(0, 255, 255, 255), Shader.TileMode.MIRROR));

		greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		greenPaint.setShader(new RadialGradient(width / 2, height * 4 / 5,
				m_radius * 3 / 2, Color.rgb(100, 255, 100), Color
						.rgb(0, 120, 0), Shader.TileMode.MIRROR));

		redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		redPaint.setShader(new RadialGradient(width / 2, height * 4 / 5,
				m_radius * 3 / 2, Color.rgb(255, 100, 100), Color
						.rgb(110, 0, 0), Shader.TileMode.MIRROR));

		grayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		grayPaint.setShader(new RadialGradient(width / 2, height * 4 / 5,
				m_radius * 3 / 2, Color.rgb(151, 151, 151), Color
						.rgb(40, 40, 40), Shader.TileMode.MIRROR));

		iconPaint = greenPaint;
	}

	public RecordingIcon(Context context) {
		super(context);
	}

	public RecordingIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecordingIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void SetRecordingMode(int mode) {
		switch (mode) {
		case 0:
			iconPaint = grayPaint;
			break;
		case 1:
			iconPaint = greenPaint;
			break;
		default:
			iconPaint = lightPaint;
			break;
		}

		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		canvas.drawCircle(width / 2, height / 2, m_radius - 4, iconPaint);
		canvas.drawCircle(width / 2, height / 2 - 3, m_radius - 4 - 4,
				lightPaint);
	}
}
