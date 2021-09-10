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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts
{
	private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
	
	private final L2PcInstance _owner;
	// 10 pages by 12 slots
	private L2ShortCut[][] _shortCuts = new L2ShortCut[11][13];
	
	public ShortCuts(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public synchronized L2ShortCut[] getAllShortCuts()
	{
		List<L2ShortCut> toReturn = new ArrayList<>();
		for (L2ShortCut[] inpage : _shortCuts)
		{
			for (L2ShortCut l2ShortCuts : inpage)
			{
				if (l2ShortCuts != null)
				{
					toReturn.add(l2ShortCuts);
				}
			}
		}
		return toReturn.toArray(new L2ShortCut[toReturn.size()]);
	}
	
	public void updateShortCuts()
	{
		ThreadPoolManager.getInstance().executeTask(() ->
		{
			L2ShortCut[] allShortCuts = getAllShortCuts();
			for (L2ShortCut l2ShortCut : allShortCuts)
			{
				if ((l2ShortCut.getType() != L2ShortCut.TYPE_SKILL) || (_owner == null))
				{
					continue;
				}
				int skillLevel = _owner.getSkillLevel(l2ShortCut.getId());
				if (skillLevel == l2ShortCut.getLevel())
				{
					continue;
				}
				
				// update shortcut
				registerShortCut(new L2ShortCut(l2ShortCut, skillLevel));
			}
			_owner.sendPacket(new ShortCutInit(_owner));
		});
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		// L2ShortCut sc = _shortCuts.get(slot + (page * 12));
		L2ShortCut sc = _shortCuts[page][slot];
		
		// verify shortcut
		if ((sc != null) && (sc.getType() == L2ShortCut.TYPE_ITEM))
		{
			if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
				sc = null;
			}
		}
		
		return sc;
	}
	
	public synchronized void registerShortCut(L2ShortCut shortcut)
	{
		L2ShortCut oldShortCut = _shortCuts[shortcut.getPage()][shortcut.getSlot()];
		_shortCuts[shortcut.getPage()][shortcut.getSlot()] = shortcut;
		registerShortCutInDb(shortcut, oldShortCut);
	}
	
	private void registerShortCutInDb(final L2ShortCut shortcut, final L2ShortCut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warning("Could not store character shortcut: " + e1);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e2)
			{
			}
		}
	}
	
	/**
	 * @param slot
	 * @param page
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		L2ShortCut old = _shortCuts[page][slot];
		
		if ((old == null) || (_owner == null))
		{
			return;
		}
		
		deleteShortCutFromDb(old);
		if (old.getType() == L2ShortCut.TYPE_ITEM)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if ((item != null) && (item.getItemType() == L2EtcItemType.SHOT))
			{
				_owner.removeAutoSoulShot(item.getItemId());
				_owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
			}
		}
		
		// finally remove
		_shortCuts[page][slot] = null;
		
		// send to init
		_owner.sendPacket(new ShortCutInit(_owner));
		
		// update shoulshoit info
		for (int shotId : _owner.getAutoSoulShot().values())
		{
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		L2ShortCut toRemove = null;
		
		for (L2ShortCut shortcut : getAllShortCuts())
		{
			if ((shortcut.getType() == L2ShortCut.TYPE_ITEM) && (shortcut.getId() == objectId))
			{
				toRemove = shortcut;
				break;
			}
		}
		
		if (toRemove != null)
		{
			deleteShortCut(toRemove.getSlot(), toRemove.getPage());
		}
	}
	
	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(final L2ShortCut shortcut)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warning("Could not delete character shortcut: " + e1);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e2)
			{
			}
		}
	}
	
	public void restore()
	{
		_shortCuts = new L2ShortCut[11][13];
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, _owner.getClassIndex());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				int page = rset.getInt("page");
				int type = rset.getInt("type");
				int id = rset.getInt("shortcut_id");
				int level = rset.getInt("level");
				
				L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);
				_shortCuts[page][slot] = sc;
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore character shortcuts: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		
		// verify shortcuts
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc.getType() == L2ShortCut.TYPE_ITEM)
			{
				if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
			}
		}
	}
}
