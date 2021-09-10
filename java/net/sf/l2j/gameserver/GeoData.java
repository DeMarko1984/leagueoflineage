/* This program is free software; you can redistribute it and/or modify
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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;

/**
 * @author -Nemesiss-
 */
public class GeoData
{
	private static Logger _log = Logger.getLogger(GeoData.class.getName());
	private static GeoData _instance;
	
	public static GeoData getInstance()
	{
		if (_instance == null)
		{
			if (Config.GEODATA > 0)
			{
				_instance = GeoEngine.getInstance();
			}
			else
			{
				_instance = new GeoData();
				_log.info("Geodata Engine: Disabled.");
			}
		}
		return _instance;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public int getHeight(int x, int y, int z)
	{
		return z;
	}
	
	/**
	 * @param cha
	 * @param target
	 * @return
	 */
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		return true;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param tx
	 * @param ty
	 * @param tz
	 * @param instanceId
	 * @return
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		return new Location(tx, ty, tz);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public int getSpawnHeight(int x, int y, int z)
	{
		return z;
	}
	
	/**
	 * @param location
	 * @return
	 */
	public int getSpawnHeight(Location location)
	{
		return location.getZ();
	}
	
	/**
	 * @param newlocx
	 * @param newlocy
	 * @param zmin
	 * @param zmax
	 * @param _id
	 * @return
	 */
	public int getSpawnHeight(int newlocx, int newlocy, int zmin, int zmax, int _id)
	{
		return getSpawnHeight(newlocx, newlocy, zmax);
	}
}
