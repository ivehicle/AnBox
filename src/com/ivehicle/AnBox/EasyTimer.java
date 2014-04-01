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

import com.ivehicle.util.Log;

import android.os.Handler;

public abstract class EasyTimer
{
	private final String m_className = "EasyTimer";
	
	private Handler m_handler;
	private long m_timerCycle;
	private TimerRunnable m_runnable;
	private Boolean m_isRunning = false;

	class TimerRunnable implements Runnable
	{
		private final String m_className = "TimerRunnable";

		public void run()
		{
			Log.i(m_className, "run() is called.");
			if( m_isRunning )
				m_handler.postDelayed(this, m_timerCycle);
			doRun();			
		}
	}
	
	public EasyTimer(long cycle)
	{
		Log.i(m_className, "EasyTimer() is called.");
		m_handler = new Handler();
		m_runnable = new TimerRunnable();
		m_timerCycle = cycle;
	}

	public void ChangeCycle(int cycle)
	{
		Log.i(m_className, "ChangeCycle() is called.");
		m_timerCycle = cycle;
	}

	public void Start()
	{
		Log.i(m_className, "Start() is called.");
		m_isRunning = true;
		m_handler.postDelayed(m_runnable, m_timerCycle);
	}
	
	public void Stop()
	{
		m_isRunning = false;
	}

	abstract protected void doRun();
}
