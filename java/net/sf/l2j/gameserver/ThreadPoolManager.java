/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.mmocore.network.ReceivablePacket;

public class ThreadPoolManager
{
	private static ThreadPoolManager _instance;
	private final ScheduledExecutorService _generalScheduledThreadPool;
	private final ExecutorService _generalThreadPool;
	private boolean _shutdown;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ThreadPoolManager();
		}
		return _instance;
	}
	
	private ThreadPoolManager()
	{
		int cores = Runtime.getRuntime().availableProcessors() * 2;
		_generalScheduledThreadPool = Executors.newScheduledThreadPool(cores);
		System.out.println("Start Scheduled executorvvservice service for " + cores + " threads.");
		_generalThreadPool = Executors.newFixedThreadPool(cores);
		System.out.println("Start executor service for " + cores + " threads.");
	}
	
	public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (delay == 0)
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (initial < 0)
			{
				initial = 0;
			}
			if ((delay == 0) && (initial == 0))
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (delay == 0)
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (initial < 0)
			{
				initial = 0;
			}
			if ((delay == 0) && (initial == 0))
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (delay == 0)
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0)
			{
				delay = 0;
			}
			if (initial < 0)
			{
				initial = 0;
			}
			if ((delay == 0) && (initial == 0))
			{
				_generalThreadPool.execute(r);
				return null;
			}
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}
	
	public void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		_generalThreadPool.execute(pkt);
	}
	
	public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
	{
		_generalThreadPool.execute(pkt);
	}
	
	public void executeTask(Runnable r)
	{
		_generalThreadPool.execute(r);
	}
	
	public void executeAi(Runnable r)
	{
		_generalThreadPool.execute(r);
	}
	
	public String[] getStats()
	{
		return null;
	}
	
	/**
	 *
	 */
	public void shutdown()
	{
		_shutdown = true;
		try
		{
			_generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalScheduledThreadPool.shutdown();
			_generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			_generalThreadPool.shutdown();
			System.out.println("All ThreadPools are now stoped");
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	/**
	 *
	 */
	public void purge()
	{
	}
	
	/**
	 * @return
	 */
	public String getPacketStats()
	{
		TextBuilder tb = new TextBuilder();
		return tb.toString();
	}
}