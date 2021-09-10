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
package net.sf.l2j.gameserver.clientpackets;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

/**
 * This class ...
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
	
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
		{
			if (Config.DEBUG)
			{
				_log.fine("Max number of characters reached. Creation failed.");
			}
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
			sendPacket(ccf);
			return;
		}
		else if (CharNameTable.getInstance().doesCharNameExist(_name))
		{
			if (Config.DEBUG)
			{
				_log.fine("charname: " + _name + " already exists. creation failed.");
			}
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			sendPacket(ccf);
			return;
		}
		else if ((_name.length() < 3) || (_name.length() > 16) || !Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
			{
				_log.fine("charname: " + _name + " is invalid. creation failed.");
			}
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("charname: " + _name + " classId: " + _classId);
		}
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
		if ((template == null) || (template.classBaseLevel > 1))
		{
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
			sendPacket(ccf);
			return;
		}
		
		int objectId = IdFactory.getInstance().getNextId();
		L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);
		newChar.setCurrentHp(template.baseHpMax);
		newChar.setCurrentCp(template.baseCpMax);
		newChar.setCurrentMp(template.baseMpMax);
		// newChar.setMaxLoad(template.baseLoad);
		
		// send acknowledgement
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);
		
		initNewChar(getClient(), newChar);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			_log.fine("Character init start");
		}
		L2World.getInstance().storeObject(newChar);
		
		L2PcTemplate template = newChar.getTemplate();
		
		// newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		// newChar.addExpAndSp(Experience.LEVEL[21], 100000);
		newChar.setTitle("");
		
		L2ShortCut shortcut;
		// add attack shortcut
		shortcut = new L2ShortCut(0, 0, 3, 2, -1, 1);
		newChar.registerShortCut(shortcut);
		// add take shortcut
		shortcut = new L2ShortCut(3, 0, 3, 5, -1, 1);
		newChar.registerShortCut(shortcut);
		// add sit shortcut
		shortcut = new L2ShortCut(10, 0, 3, 0, -1, 1);
		newChar.registerShortCut(shortcut);
		
		ItemTable.getInstance();
		L2Item[] items = template.getItems();
		for (L2Item item2 : items)
		{
			if (item2.getItemId() == 5588)
			{
				continue;
			}
			L2ItemInstance item = newChar.getInventory().addItem("Init", item2.getItemId(), 1, newChar, null);
			if (item.isEquipable())
			{
				if ((newChar.getActiveWeaponItem() == null) || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
				{
					newChar.getInventory().equipItemAndRecord(item);
				}
			}
		}
		
		L2GameClient.saveCharToDisk(newChar);
		// send char list
		CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		if (Config.DEBUG)
		{
			_log.fine("Character init end");
		}
		
		// every new char spawn as random position of all monsters not aggresable
		int spawnX = template.spawnX;
		int spawnY = template.spawnY;
		int spawnZ = template.spawnZ;
		int _spawnX = template.spawnX;
		int _spawnY = template.spawnY;
		int _spawnZ = template.spawnZ;
		Iterator<L2Spawn> spawnTable = SpawnTable.getInstance().getSpawnTable().values().iterator();
		while (spawnTable.hasNext())
		{
			L2Spawn l2Spawn = spawnTable.next();
			L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(l2Spawn.getNpcid());
			if ((npcTemplate.aggroRange > 0) || (Rnd.get(1000) >= 1) || (npcTemplate.level > 20) || (npcTemplate.level < 5))
			{
				continue;
			}
			spawnX = l2Spawn.getLocx();
			spawnY = l2Spawn.getLocy();
			spawnZ = l2Spawn.getLocz();
			// skip katakombs
			if ((spawnX >= 43100) && (spawnY >= 246500) && (spawnX <= 49400) && (spawnY <= 249200))
			{
				continue;
			}
			
			if ((spawnX >= -49800) && (spawnY >= 43790) && (spawnX <= -35311) && (spawnY <= 56879))
			{
				continue;
			}
			if ((spawnX >= -54659) && (spawnY >= 41782) && (spawnX <= -47150) && (spawnY <= 53065))
			{
				continue;
			}
			if ((spawnX >= -55186) && (spawnY >= 55152) && (spawnX <= -38741) && (spawnY <= 62474))
			{
				continue;
			}
			if ((spawnX >= -45452) && (spawnY >= 165166) && (spawnX <= 917) && (spawnY <= 201937))
			{
				continue;
			}
			if ((spawnX >= 6068) && (spawnY >= 69188) && (spawnX <= 36734) && (spawnY <= 88790))
			{
				continue;
			}
			if ((spawnX >= 76563) && (spawnY >= 7238) && (spawnX <= 98577) && (spawnY <= 27040))
			{
				continue;
			}
			if ((spawnX >= 98542) && (spawnY >= 92245) && (spawnX <= 123457) && (spawnY <= 68112))
			{
				continue;
			}
			if ((spawnX >= -7877) && (spawnY >= 8905) && (spawnX <= 34061) && (spawnY <= 26384))
			{
				continue;
			}
			if ((spawnX >= -31364) && (spawnY >= 131977) && (spawnX <= -1231) && (spawnY <= 160147))
			{
				continue;
			}
			if ((spawnX >= -77276) && (spawnY >= 120547) && (spawnX <= -69590) && (spawnY <= 134858))
			{
				continue;
			}
			if ((spawnX >= -69590) && (spawnY >= 120547) && (spawnX <= 38116) && (spawnY <= 147264))
			{
				continue;
			}
			_spawnX = spawnX;
			_spawnY = spawnY;
			_spawnZ = spawnZ;
			break;
		}
		
		newChar.setXYZInvisible(_spawnX, _spawnY, _spawnZ);
		
		// add all recipies to all new characters
		newChar.addSkill(SkillTable.getInstance().getInfo(1322, SkillTable.getInstance().getMaxLevel(1322, 1)), true);
		newChar.addSkill(SkillTable.getInstance().getInfo(1321, SkillTable.getInstance().getMaxLevel(1321, 1)), true);
		newChar.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_CREATE_DWARVEN, Math.max(SkillTable.getInstance().getMaxLevel(L2Skill.SKILL_CREATE_DWARVEN, 1) - 2, 1)), true);
		newChar.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_CREATE_COMMON, Math.max(SkillTable.getInstance().getMaxLevel(L2Skill.SKILL_CREATE_COMMON, 1) - 2, 1)), true);
		newChar.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_CRYSTALLIZE, SkillTable.getInstance().getMaxLevel(L2Skill.SKILL_CRYSTALLIZE, 1)), true);
		
		// auto learn skills
		newChar.giveAvailableSkills(false);
		
		// double store character
		L2GameClient.saveCharToDisk(newChar);
		
		newChar.deleteMe(); // release the world of this character and it's inventory
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}
