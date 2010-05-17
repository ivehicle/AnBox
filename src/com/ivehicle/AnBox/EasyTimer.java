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