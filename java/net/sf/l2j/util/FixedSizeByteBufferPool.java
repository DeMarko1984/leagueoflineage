/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author i.muratov
 */
public class FixedSizeByteBufferPool
{
	private final ByteBuffer[] _data;
	private byte _actualsize;
	private final int HELPER_BUFFER_SIZE;
	private final ByteOrder BYTE_ORDER;
	
	public FixedSizeByteBufferPool(byte size, int hELPER_BUFFER_SIZE, ByteOrder byteOrder)
	{
		HELPER_BUFFER_SIZE = hELPER_BUFFER_SIZE;
		BYTE_ORDER = byteOrder;
		_actualsize = size;
		
		_data = new ByteBuffer[size];
		for (int j = 0; j < _data.length; j++)
		{
			_data[j] = ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
		}
	}
	
	public ByteBuffer remove()
	{
		ByteBuffer pool = null;
		synchronized (_data)
		{
			if (!isEmpty())
			{
				for (byte i = 0; i < _data.length; i++)
				{
					if (_data[i] != null)
					{
						pool = _data[i];
						_data[i] = null;
						_actualsize -= 1;
						break;
					}
				}
			}
		}
		return pool;
	}
	
	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return _actualsize == 0;
	}
	
	/**
	 * @param buf
	 */
	public void recycleBuffer(ByteBuffer buf)
	{
		synchronized (_data)
		{
			if (_actualsize < _data.length)
			{
				for (byte i = 0; i < _data.length; i++)
				{
					if (_data[i] == null)
					{
						buf.clear();
						_data[i] = buf;
						_actualsize += 1;
					}
				}
			}
		}
	}
	
	/**
	 * @param hELPER_BUFFER_SIZE
	 * @param byteOrder
	 */
	public void Set(int hELPER_BUFFER_SIZE, ByteOrder byteOrder)
	{
		// TODO Auto-generated method stub
		
	}
}
