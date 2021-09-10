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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.lge.LgeEvent;
import net.sf.l2j.gameserver.lge.LgeManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2TvTBoxInstance extends L2NpcInstance
{
	private LgeEvent _currentEvent;
	
	public L2TvTBoxInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance#onAction(net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		LgeEvent currentEvent = getCurrentEvent();
		LgeEvent currentTvTEvent = player.getCurrentLgeEvent();
		if ((currentTvTEvent != null) && (currentEvent != null) && (currentTvTEvent == currentEvent))
		{
			LgeManager.getInstance().incPoints(player);
			this.reduceCurrentHp(this.getMaxHp() + 1, player);
			return;
		}
		super.onAction(player);
	}
	
	/**
	 * @return the _currentEvent
	 */
	public LgeEvent getCurrentEvent()
	{
		return _currentEvent;
	}
	
	/**
	 * @param _currentEvent the _currentEvent to set
	 */
	public void setCurrentEvent(LgeEvent _currentEvent)
	{
		this._currentEvent = _currentEvent;
	}
}
