/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Util
{
	public static boolean isInternalIP(String ipAddress)
	{
		return (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") ||
		// ipAddress.startsWith("172.16.") ||
		// Removed because there are some net IPs in this range.
		// TODO: Use regexp or something to only include 172.16.0.0 => 172.16.31.255
			ipAddress.startsWith("127.0.0.1"));
	}
	
	public static String printData(byte[] data, int len)
	{
		TextBuilder result = new TextBuilder();
		
		int counter = 0;
		
		for (int i = 0; i < len; i++)
		{
			if ((counter % 16) == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				
				int charpoint = i - 15;
				for (int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if ((t1 > 0x1f) && (t1 < 0x80))
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}
				
				result.append("\n");
				counter = 0;
			}
		}
		
		int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); i++)
			{
				result.append("   ");
			}
			
			int charpoint = data.length - rest;
			for (int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if ((t1 > 0x1f) && (t1 < 0x80))
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			
			result.append("\n");
		}
		
		return result.toString();
	}
	
	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		
		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		
		return number;
	}
	
	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}
	
	public static String getExternalHost()
	{
		// get external ip using serivice https://api.ipify.org/
		URLConnection connection = null;
		try
		{
			connection = new URL("https://api.ipify.org/").openConnection();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		InputStream is = null;
		try
		{
			is = connection.getInputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		InputStreamReader reader = new InputStreamReader(is);
		char[] buffer = new char[256];
		int rc;
		
		StringBuilder sb = new StringBuilder();
		
		String externalp = null;
		try
		{
			while ((rc = reader.read(buffer)) != -1)
			{
				sb.append(buffer, 0, rc);
			}
			externalp = sb.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return externalp;
	}
	
}
