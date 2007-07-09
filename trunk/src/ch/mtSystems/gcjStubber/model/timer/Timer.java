/*
 *   GcjStubber - A stub creator for GCJ (JNC).
 *   Copyright (C) 2007  Marco Trudel <mtrudel@gmx.ch>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.mtSystems.gcjStubber.model.timer;

import java.util.LinkedList;
import java.util.List;


/**
 * A self adjusting (lag preventing) timer that allows to be notified in
 * requested intervals.  
 */
public class Timer
{
	private List<TickListener> listeners = new LinkedList<TickListener>();
	private int sleep;
	private boolean stop = false;
	private Thread thread;


	/**
	 * Create a new timer.
	 * 
	 * @param sleep The time to sleep between the ticks in milliseconds.
	 */
	public Timer(int sleep)
	{
		this.sleep = sleep;
	}


	// --------------- public methods ---------------

	/**
	 * Add a tick listener.
	 * 
	 * @param tickListener The tick listener.
	 */
	public void addTickListener(TickListener tickListener)
	{
		listeners.add(tickListener);
	}

	/**
	 * Start the timer.
	 */
	public void start()
	{
		thread = new Thread()
		{
			public void run()
			{
				long wakeUpAbsolute = System.currentTimeMillis();
				while(!stop)
				{
					try
					{
						wakeUpAbsolute += sleep;
						long fixedSleep = wakeUpAbsolute - System.currentTimeMillis();
						if(fixedSleep > 0) Thread.sleep(fixedSleep);
						for(TickListener l : listeners) l.tick();
					} catch(InterruptedException ex) { }
				}
				stop = false;
				thread = null;
			}
		};
		thread.start();
	}
	
	/**
	 * Stop the timer. Please note that this method does not block.
	 * It's possible that one more tick will be sent after issuing a stop.
	 */
	public void stop()
	{
		stop = true;
		thread.interrupt();
	}
}
