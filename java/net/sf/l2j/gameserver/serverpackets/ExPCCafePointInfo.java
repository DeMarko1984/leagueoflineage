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
package net.sf.l2j.gameserver.serverpackets;

/**
 * Format: ch ddcdc
 * @author KenM
 */
public class ExPCCafePointInfo extends L2GameServerPacket
{
	private static final String _S__FE_31_EXPCCAFEPOINTINFO = "[S] FE:31 ExPCCafePointInfo";
	private final int _score, _modify, _periodType, _remainingTime;
	private int _pointType = 0;
	
	public ExPCCafePointInfo(int score, int modify, boolean addPoint, boolean pointType, int remainingTime)
	{
		_score = score;
		_modify = addPoint ? modify : modify * -1;
		_remainingTime = remainingTime;
		_pointType = addPoint ? (pointType ? 0 : 1) : 2;
		_periodType = 1; // get point time
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_score);
		writeD(_modify);
		writeC(_periodType);
		writeD(_remainingTime);
		writeC(_pointType);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_31_EXPCCAFEPOINTINFO;
	}
}
