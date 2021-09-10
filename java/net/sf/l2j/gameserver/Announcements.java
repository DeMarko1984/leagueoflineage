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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.7 $ $Date: 2005/03/29 23:15:14 $
 */
public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());
	
	private static Announcements _instance;
	private final List<String> _announcements = new ArrayList<>();
	private final List<List<Object>> _eventAnnouncements = new ArrayList<>();
	
	public Announcements()
	{
		loadAnnouncements();
	}
	
	public static Announcements getInstance()
	{
		if (_instance == null)
		{
			_instance = new Announcements();
		}
		
		return _instance;
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.config("data/announcements.txt doesn't exist");
		}
	}
	
	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcements.get(i));
			activeChar.sendPacket(cs);
		}
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		List<Object> entry = new ArrayList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}
	
	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		TextBuilder replyMSG = new TextBuilder("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(File file)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			
			String st;
			while ((st = br.readLine()) != null)
			{
				if (st.trim().length() > 0)
				{
					_announcements.add(st.trim());
				}
			}
			
			_log.config("Announcements: Loaded " + _announcements.size() + " Announcements.");
		}
		catch (IOException e1)
		{
			_log.log(Level.SEVERE, "Error reading announcements", e1);
		}
		
		try
		{
			br.close();
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Error reading announcements", e);
		}
	}
	
	private void saveToDisk()
	{
		File file = new File("data/announcements.txt");
		FileWriter save = null;
		
		try
		{
			save = new FileWriter(file);
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
			save.flush();
			save.close();
			save = null;
		}
		catch (IOException e)
		{
			_log.warning("saving the announcements file has failed: " + e);
		}
	}
	
	public void announceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	
	public void announceToAll(SystemMessage sm)
	{
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}
	
	// Method fo handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			Announcements.getInstance().announceToAll(text);
		}
		
		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
}
