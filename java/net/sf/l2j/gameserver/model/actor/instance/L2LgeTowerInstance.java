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
package net.sf.l2j.gameserver.model.actor.instance;

import java.lang.ref.WeakReference;

import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.lge.LgeEvent;
import net.sf.l2j.gameserver.lge.LgeEventTeam;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2LgeTowerInstance extends L2NpcInstance
{
	public static class L2LgeTowerHealTask implements Runnable
	{
		
		private final WeakReference<L2LgeTowerInstance> _npc;
		private final WeakReference<LgeEvent> _event;
		private final WeakReference<LgeEventTeam> _team;
		
		/**
		 * @param npc
		 * @param event
		 * @param team
		 */
		public L2LgeTowerHealTask(L2LgeTowerInstance npc, LgeEvent event, LgeEventTeam team)
		{
			_npc = new WeakReference<>(npc);
			_event = new WeakReference<>(event);
			_team = new WeakReference<>(team);
			
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
		
		@Override
		public void run()
		{
			if ((_team.get() == null) || (_event.get() == null) || (_npc.get() == null))
			{
				return;
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			
			L2PcInstance[] knownPlayersInRadius = _npc.get().getKnownList().getKnownPlayersInRadius(250);
			for (L2PcInstance l2PcInstance : knownPlayersInRadius)
			{
				if (l2PcInstance == null)
				{
					continue;
				}
				if (l2PcInstance.getTeam() != _team.get().getId())
				{
					continue;
				}
				if (l2PcInstance.getCurrentLgeEvent() != _event.get())
				{
					continue;
				}
				if ((l2PcInstance.getCurrentHp() >= (l2PcInstance.getMaxHp() * 0.7)) || l2PcInstance.isDead() || l2PcInstance.isAlikeDead())
				{
					continue;
				}
				
				// recover 10% of max HP
				l2PcInstance.setCurrentHp(l2PcInstance.getCurrentHp() + (l2PcInstance.getMaxHp() * 0.10));
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
				sm.addNumber((int) (l2PcInstance.getMaxHp() * 0.10));
				l2PcInstance.sendPacket(sm);
			}
		}
		
	}
	
	private WeakReference<LgeEvent> __event;
	private WeakReference<LgeEventTeam> __team;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2LgeTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAttackable()
	{
		return (__event != null) && (__event.get() != null) && __event.get().isStarted();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (!(attacker instanceof L2PcInstance))
		{
			return false;
		}
		return (__event != null) && (__event.get() != null) && __event.get().isStarted() && (__team != null) && (__team.get() != null)
			&& !__team.get().containsPlayer(((L2PcInstance) attacker).getName());
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if ((player == null) || !canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(new ActionFailed());
		}
		else
		{
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100 // Less then max height difference, delete check when geo
			) && GeoData.getInstance().canSeeTarget(player, this))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(new ActionFailed());
			}
		}
	}
	
	public void onDeath()
	{
		if ((__event == null) || (__event.get() == null))
		{
			return;
		}
		if (!__event.get().isStarted())
		{
			return;
		}
		if ((__team == null) || (__team.get() == null))
		{
			return;
		}
		
		// decrease points by 2x
		__team.get().decreasePoints();
		
		// broadcast messsage
		SystemMessage sm = new SystemMessage(SystemMessageId.LOL_TOWER_KILLED);
		sm.addString(__team.get().getName());
		__event.get().broadсastMessage(sm);
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	public void setEvent(LgeEvent event)
	{
		__event = new WeakReference<>(event);
	}
	
	public void setTeam(LgeEventTeam team)
	{
		System.out.println(team.getName());
		System.out.println(team.getId());
		__team = new WeakReference<>(team);
	}
}
