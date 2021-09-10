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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
 * This class handles following admin commands:
 * <li>add_exp_sp_to_character <i>shows menu for add or remove</i>
 * <li>add_exp_sp exp sp <i>Adds exp & sp to target, displays menu if a parameter is missing</i>
 * <li>remove_exp_sp exp sp <i>Removes exp & sp from target, displays menu if a parameter is missing</i>
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminExpSp implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminExpSp.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_exp_sp_to_character",
		"admin_add_exp_sp",
		"admin_remove_exp_sp",
		"admin_add_henna",
		"admin_clear_henna"
	};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), "");
		if (command.startsWith("admin_add_exp_sp"))
		{
			try
			{
				String val = command.substring(16);
				if (!adminAddExpSp(activeChar, val))
				{
					activeChar.sendMessage("Usage: //add_exp_sp exp sp");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //add_exp_sp exp sp");
			}
			addExpSp(activeChar);
		}
		else if (command.startsWith("admin_remove_exp_sp"))
		{
			try
			{
				String val = command.substring(19);
				if (!adminRemoveExpSP(activeChar, val))
				{
					activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
			}
			addExpSp(activeChar);
		}
		else if (command.startsWith("admin_add_henna"))
		{
			try
			{
				String val = command.substring("admin_add_henna ".length());
				if (!adminAddHenna(activeChar, val))
				{
					activeChar.sendMessage("Usage: //admin_add_henna [str|dex|con|men|wit|int] count <max 99>");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //admin_add_henna [str|dex|con|men|wit|int] count <max 99>");
			}
		}
		else if (command.startsWith("admin_clear_henna"))
		{
			try
			{
				if (!adminClearHenna(activeChar))
				{
					activeChar.sendMessage("Usage: //admin_clear_henna");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //admin_clear_henna");
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	private void addExpSp(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/expsp.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		activeChar.sendPacket(adminReply);
	}
	
	private boolean adminAddHenna(L2PcInstance activeChar, String HennaCount)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		StringTokenizer st = new StringTokenizer(HennaCount);
		if (st.countTokens() != 2)
		{
			return false;
		}
		
		String type = st.nextToken();
		String count = st.nextToken();
		int countval = 0;
		try
		{
			countval = Integer.parseInt(count);
		}
		catch (Exception e)
		{
			return false;
		}
		if ((type != "") || (countval > 0))
		{
			// Common character information
			switch (type)
			{
				case "str":
					player.AddSTR(countval);
					break;
				case "con":
					player.AddCON(countval);
					break;
				case "dex":
					player.AddDEX(countval);
					break;
				case "men":
					player.AddMEN(countval);
					break;
				case "int":
					player.AddINT(countval);
					break;
				case "wit":
					player.AddWIT(countval);
					break;
				default:
					return false;
			}
			UserInfo ui = new UserInfo(player);
			player.sendPacket(ui);
			player.sendMessage("Admin is adding you [" + type + "] as " + countval + ".");
			player.store();
			activeChar.sendMessage("Added [" + type + "] as  " + countval + ".");
			if (Config.DEBUG)
			{
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added [" + type + "] as " + countval + "to " + player.getObjectId() + ".");
			}
		}
		return true;
	}
	
	private boolean adminClearHenna(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		player.clearHenna();
		UserInfo ui = new UserInfo(player);
		player.sendPacket(ui);
		player.sendMessage("Admin is clear you henna.");
		player.store();
		activeChar.sendMessage("Cclear henna " + activeChar.getName() + ".");
		if (Config.DEBUG)
		{
			_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") clear henna to " + player.getObjectId() + ".");
		}
		return true;
	}
	
	private boolean adminAddExpSp(L2PcInstance activeChar, String ExpSp)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		StringTokenizer st = new StringTokenizer(ExpSp);
		if (st.countTokens() != 2)
		{
			return false;
		}
		
		String exp = st.nextToken();
		String sp = st.nextToken();
		long expval = 0;
		int spval = 0;
		try
		{
			expval = Long.parseLong(exp);
			spval = Integer.parseInt(sp);
		}
		catch (Exception e)
		{
			return false;
		}
		if ((expval != 0) || (spval != 0))
		{
			// Common character information
			player.sendMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
			player.addExpAndSp(expval, spval);
			// Admin information
			activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
			if (Config.DEBUG)
			{
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added " + expval + " xp and " + spval + " sp to " + player.getObjectId() + ".");
			}
		}
		return true;
	}
	
	private boolean adminRemoveExpSP(L2PcInstance activeChar, String ExpSp)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		StringTokenizer st = new StringTokenizer(ExpSp);
		if (st.countTokens() != 2)
		{
			return false;
		}
		
		String exp = st.nextToken();
		String sp = st.nextToken();
		long expval = 0;
		int spval = 0;
		try
		{
			expval = Long.parseLong(exp);
			spval = Integer.parseInt(sp);
		}
		catch (Exception e)
		{
			return false;
		}
		if ((expval != 0) || (spval != 0))
		{
			// Common character information
			player.sendMessage("Admin is removing you " + expval + " xp and " + spval + " sp.");
			player.removeExpAndSp(expval, spval);
			// Admin information
			activeChar.sendMessage("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
			if (Config.DEBUG)
			{
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") removed " + expval + " xp and " + spval + " sp from " + player.getObjectId() + ".");
			}
		}
		return true;
	}
}
