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

import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ExPCCafePointInfo;
import net.sf.l2j.gameserver.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.serverpackets.FriendList;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.inmem.L2JInMemDatabase;
import net.sf.l2j.inmem.chemas.L2CharacterFriend;
import net.sf.l2j.util.Rnd;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev656 cbdddd
 * <p>
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}
	
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		// Register in flood protector
		FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				_log.warning("User already exist in OID map! User " + activeChar.getName() + " is character clone");
				// activeChar.closeNetConnection();
			}
		}
		
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_GODMODE))
				|| (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul"))))
			{
				activeChar.setIsInvul(true);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_GODMODE))
				|| (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible"))))
			{
				activeChar.getAppearance().setInvisible();
			}
			
			if (Config.GM_STARTUP_SILENCE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_MENU))
				|| (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence"))))
			{
				activeChar.setMessageRefusal(true);
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_MENU))
				|| (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston"))))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}
			
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (activeChar.getAccessLevel() >= 100)
				{
					activeChar.getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
				}
				else if (activeChar.getAccessLevel() >= 75)
				{
					activeChar.getAppearance().setNameColor(Config.GM_NAME_COLOR);
				}
			}
		}
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		// buff and status icons
		if (Config.STORE_SKILL_COOLTIME)
		{
			activeChar.restoreEffects();
		}
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// engage and notify Partner
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}
		
		if (activeChar.getAllEffects() != null)
		{
			for (L2Effect e : activeChar.getAllEffects())
			{
				if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
				
				if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
			}
		}
		
		// apply augmentation boni for equipped items
		for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
		{
			if ((temp != null) && temp.isEquipped())
			{
				temp.getAugmentation().applyBoni(activeChar);
			}
		}
		
		// Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);
		
		activeChar.getMacroses().sendUpdate();
		
		sendPacket(new UserInfo(activeChar));
		
		sendPacket(new HennaInfo(activeChar));
		
		sendPacket(new FriendList(activeChar));
		
		sendPacket(new ItemList(activeChar, false));
		
		sendPacket(new ShortCutInit(activeChar));
		
		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);
		
		Announcements.getInstance().showAnnouncements(activeChar);
		
		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());
		
		if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm("data/html/servnews.htm");
			if (serverNews != null)
			{
				sendPacket(new NpcHtmlMessage(1, serverNews));
			}
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		// send user info again .. just like the real client
		// sendPacket(ui);
		
		L2Clan clan = activeChar.getClan();
		if ((activeChar.getClanId() != 0) && (clan != null))
		{
			sendPacket(new PledgeShowMemberListAll(clan, activeChar));
			sendPacket(new PledgeStatusChanged(clan));
		}
		
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}
		
		if (Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}
		
		// if ((Hero.getInstance().getHeroes() != null) && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
		// {
		// activeChar.setHero(true);
		// }
		
		// force all characters noble
		// activeChar.setNoble(true);
		
		// set hero if in top
		// String[] topPlayers = LgeManager.getInstance().getTopPlayers();
		// for (String name : topPlayers)
		// {
		// if (activeChar.getName().equalsIgnoreCase(name))
		// {
		// activeChar.setHero(true);
		// System.out.println(activeChar + " is in Top, set as Hero!");
		// }
		// }
		
		setPledgeClass(activeChar);
		
		// add char to online characters
		activeChar.setOnlineStatus(true);
		
		notifyFriends(activeChar);
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);
		
		activeChar.onPlayerEnter();
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}
		
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
		}
		
		// activeChar.sendPacket(new SystemMessage(SystemMessageId.LOL_MASTER_STATUS_HEADER));
		// SystemMessage[] sms = LgeManager.getInstance().generateSystemMessagesTopPlayers();
		// for (SystemMessage systemMessage : sms)
		// {
		// activeChar.sendPacket(systemMessage);
		// }
		
		// if (!LgeManager.getInstance().onLogin(activeChar))
		// {
		ThreadPoolManager.getInstance().executeTask(new KnownListAsynchronousUpdateTask(activeChar));
		// }
		
		// send info for hennas
		// int canAdd = activeChar.getLevel() - activeChar.GetCurrentAlternativeHennas();
		// if (canAdd > 0)
		// {
		// sm = new SystemMessage(SystemMessageId.LOL_CAN_BE_HENNAS);
		// sm.addString(String.valueOf(canAdd));
		// activeChar.sendPacket(sm);
		// }
		
		// send lge points in enter
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		boolean mageClass = activeChar.isMageClass();
		if ((weaponItem != null) && mageClass)
		{
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getAddMagicUseSkill(), activeChar.getAddMagicUseSkill(), true, true, 24));
		}
		else if ((weaponItem != null) && !mageClass)
		{
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getAddWeaponUseSkill(weaponItem.getItemType()), activeChar.getAddWeaponUseSkill(weaponItem.getItemType()), true, true, 24));
		}
		
		// basic recipes to common craft book
		// common;mk_lesser_healing_potion;686;6926;1;[6908(2)];1060;1;[6926(1)],[57(5400000)];30;100;
		// common;mk_healing_potion;687;6927;2;[6908(2)],[6911(1)];1061;1;[6927(1)],[57(5400000)];90;100;
		// common;mk_greater_healing_potion;688;6928;5;[6908(3)],[6911(3)];1539;1;[6928(1)],[57(5400000)];150;100;
		// common;mk_antidote;689;6929;1;[6908(2)];1831;1;[6929(1)],[57(5400000)];45;100;
		// common;mk_advanced_antidote;690;6930;2;[6908(3)];1832;1;[6930(1)],[57(5400000)];90;100;
		// common;mk_bandage;691;6931;1;[6908(2)];1833;1;[6931(1)],[57(5400000)];45;100;
		// common;mk_emergency_dressing;692;6932;2;[6908(3)];1834;1;[6932(1)],[57(5400000)];90;100;
		// common;mk_quick_step_potion;693;6933;1;[6908(4)],[6911(4)];734;1;[6933(1)],[57(5400000)];60;100;
		// common;mk_swift_attack_potion;694;6934;1;[6908(5)],[6911(9)];735;1;[6934(1)],[57(5400000)];60;100;
		// common;mk_adv_quick_step_potion;695;6935;3;[6909(4)],[6911(7)];1374;1;[6935(1)],[57(5400000)];120;100;
		// common;mk_adv_swift_attack_potion;696;6936;3;[6909(10)],[6911(12)];1375;1;[6936(1)],[57(5400000)];120;100;
		// common;mk_potion_of_acumen2;697;6937;1;[6908(10)],[6911(7)];6035;1;[6937(1)],[57(5400000)];60;100;
		// common;mk_potion_of_acumen3;698;6938;3;[6909(10)],[6911(12)];6036;1;[6938(1)],[57(5400000)];120;100;
		int[] commonrecieps = new int[]
		{
			6926,
			6927,
			6928,
			6929,
			6930,
			6931,
			6932,
			6933,
			6934,
			6935,
			6936,
			6937,
			6938,
			6920,
			6921
		};
		for (int rId : commonrecieps)
		{
			L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(rId);
			if (rp == null)
			{
				_log.warning(rId + " not is recipe!");
				continue;
			}
			if (activeChar.hasRecipeList(rp.getId()))
			{
				continue;
			}
			activeChar.registerCommonRecipeList(rp);
		}
		
		// every enter game learn new recipe
		int getDwarfRecipeLimit = activeChar.GetDwarfRecipeLimit();
		List<Integer> allRecipesIds = RecipeController.getInstance().getAllRecipesIds();
		while (activeChar.getDwarvenRecipeBook().length < getDwarfRecipeLimit)
		{
			for (Integer rId : allRecipesIds)
			{
				if (activeChar.getDwarvenRecipeBook().length >= getDwarfRecipeLimit)
				{
					break;
				}
				L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(rId);
				if (rp == null)
				{
					_log.warning(rId + " not is recipe!");
					continue;
				}
				if (activeChar.hasRecipeList(rp.getId()) || (10 <= Rnd.get(1000)))
				{
					continue;
				}
				if (rp.isDwarvenRecipe())
				{
					activeChar.registerDwarvenRecipeList(rp);
				}
			}
		}
		
	}
	
	/**
	 * @param cha
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();
		
		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if ((cl.getPlayer1Id() == _chaid) || (cl.getPlayer2Id() == _chaid))
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
				}
				
				cha.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == _chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}
	
	/**
	 * @param cha
	 * @param partnerId
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			L2PcInstance partner;
			partner = (L2PcInstance) L2World.getInstance().findObject(cha.getPartnerId());
			
			if (partner != null)
			{
				partner.sendMessage("Your Partner has logged in");
			}
			
			partner = null;
		}
	}
	
	/**
	 * @param cha
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(cha.getName());
		
		L2CharacterFriend[] friendList = L2JInMemDatabase.getInstance().getFriends(cha.getObjectId());
		
		for (L2CharacterFriend l2CharacterFriend : friendList)
		{
			String friendName = l2CharacterFriend.getFriendName();
			L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			
			if (friend != null) // friend logged in.
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendPacket(sm);
			}
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());
			
			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());
			
			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
	
	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;
		if (activeChar.getClan() != null)
		{
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
		}
		
		if (activeChar.isNoble() && (pledgeClass < 5))
		{
			pledgeClass = 5;
		}
		
		if (activeChar.isHero())
		{
			pledgeClass = 8;
		}
		
		activeChar.setPledgeClass(pledgeClass);
	}
}
