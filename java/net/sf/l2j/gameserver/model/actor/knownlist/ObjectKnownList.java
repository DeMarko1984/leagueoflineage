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
package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

public class ObjectKnownList
{
	public static class KnownListAsynchronousUpdateTask implements Runnable
	{
		private final L2Object _obj;
		
		public KnownListAsynchronousUpdateTask(L2Object obj)
		{
			_obj = obj;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			if (_obj != null)
			{
				ObjectKnownList knownList = _obj.getKnownList();
				if (knownList != null)
				{
					knownList.updateKnownObjects();
				}
			}
		}
	}
	
	// =========================================================
	// Data Field
	private final L2Object _activeObject;
	// L2Object
	private Map<Integer, L2Object> _knownObjects;
	// L2Character
	private Map<Integer, L2Object> _knownCharacters;
	// L2NpcInstance
	private Map<Integer, L2Object> _knownNpcInstances;
	
	// for sync working
	protected final Object _lockObject = new Object();
	
	// =========================================================
	// Constructor
	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
	}
	
	// =========================================================
	// Method - Public
	public boolean addKnownObject(L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		L2Object activeObject = getActiveObject();
		if (activeObject == null)
		{
			return false;
		}
		
		if ((object == null) || (object == activeObject) || (object.getInstanceId() != getInstanceId()))
		{
			return false;
		}
		
		// Check if already know object
		if (knowsObject(object))
		{
			if (!object.isVisible())
			{
				removeKnownObject(object);
			}
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInRange(getDistanceToWatchObject(object), activeObject, object, true))
		{
			return false;
		}
		
		return (putInKnownObjects(object.getObjectId(), object) == null);
	}
	
	// =========================================================
	// Method - Private
	private final void findCloseObjects()
	{
		L2Object activeObject = getActiveObject();
		if (activeObject == null)
		{
			return;
		}
		
		boolean isActiveObjectPlayable = (activeObject instanceof L2PlayableInstance);
		
		if (isActiveObjectPlayable)
		{
			Collection<L2Object> objects = L2World.getInstance().getVisibleObjects(activeObject, getInstanceId());
			if (objects == null)
			{
				return;
			}
			
			// Go through all visible L2Object near the L2Character
			for (L2Object object : objects)
			{
				if (object == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// L2PlayableInstance sees everything
				addKnownObject(object);
				
				// Try to add active object to object's known objects
				// Only if object is a L2Character and active object is a L2PlayableInstance
				if (object instanceof L2Character)
				{
					object.getKnownList().addKnownObject(activeObject);
				}
			}
		}
		else
		{
			Collection<L2PlayableInstance> playables = L2World.getInstance().getVisiblePlayable(activeObject, getInstanceId());
			if (playables == null)
			{
				return;
			}
			
			// Go through all visible L2Object near the L2Character
			for (L2Object playable : playables)
			{
				if (playable == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// L2Character only needs to see visible L2PcInstance and L2PlayableInstance,
				// when moving. Other l2characters are currently only known from initial spawn area.
				// Possibly look into getDistanceToForgetObject values before modifying this approach...
				addKnownObject(playable);
			}
		}
	}
	
	private final void forgetObjects()
	{
		L2Object activeObject = getActiveObject();
		if (activeObject == null)
		{
			return;
		}
		
		// Go through knownObjects
		L2Object[] knownObjects = getKnownObjectArray();
		
		if ((knownObjects == null) || (knownObjects.length == 0))
		{
			return;
		}
		
		for (L2Object object : knownObjects)
		{
			if (object == null)
			{
				continue;
			}
			
			// Remove all invisible object
			// Remove all too far object
			if ((object.getInstanceId() != getInstanceId()) || !object.isVisible() || !Util.checkIfInRange(getDistanceToForgetObject(object), activeObject, object, true))
			{
				if ((object instanceof L2BoatInstance) && (activeObject instanceof L2PcInstance))
				{
					if (((L2BoatInstance) (object)).getVehicleDeparture() == null)
					{
						//
					}
					else if (((L2PcInstance) activeObject).isInBoat())
					{
						if (((L2PcInstance) activeObject).getBoat() == object)
						{
							//
						}
						else
						{
							removeKnownObject(object);
						}
					}
					else
					{
						removeKnownObject(object);
					}
				}
				else
				{
					removeKnownObject(object);
				}
			}
		}
	}
	
	// =========================================================
	// Property - Public
	public L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	/**
	 * @return
	 */
	private int getInstanceId()
	{
		L2Object l2Object = _activeObject;
		return l2Object != null ? l2Object.getInstanceId() : 0;
	}
	
	public final L2Object[] getKnownCharactersArray()
	{
		L2Object[] _toReturn = new L2Object[0];
		synchronized (_lockObject)
		{
			Collection<L2Object> _objects = getKnownCharactersMap().values();
			List<L2Object> tmp = new ArrayList<>(_objects.size());
			for (L2Object wr : _objects)
			{
				if (wr != null)
				{
					tmp.add(wr);
				}
			}
			_toReturn = tmp.toArray(new L2Object[tmp.size()]);
		}
		return _toReturn;
	}
	
	private final Map<Integer, L2Object> getKnownCharactersMap()
	{
		if (_knownCharacters == null)
		{
			_knownCharacters = new HashMap<>();
		}
		return _knownCharacters;
	}
	
	public final L2Object[] getKnownNpcInstanceArray()
	{
		L2Object[] _toReturn = new L2Object[0];
		synchronized (_lockObject)
		{
			Collection<L2Object> _objects = getKnownNpcInstanceMap().values();
			List<L2Object> tmp = new ArrayList<>(_objects.size());
			for (L2Object wr : _objects)
			{
				if ((wr != null))
				{
					tmp.add(wr);
				}
			}
			_toReturn = tmp.toArray(new L2Object[tmp.size()]);
		}
		return _toReturn;
	}
	
	private final Map<Integer, L2Object> getKnownNpcInstanceMap()
	{
		if (_knownNpcInstances == null)
		{
			_knownNpcInstances = new HashMap<>();
		}
		return _knownNpcInstances;
	}
	
	public final L2Object[] getKnownObjectArray()
	{
		L2Object[] _toReturn = new L2Object[0];
		synchronized (_lockObject)
		{
			Collection<L2Object> _objects = getKnownObjectsMap().values();
			List<L2Object> tmp = new ArrayList<>(_objects.size());
			for (L2Object wr : _objects)
			{
				if ((wr != null))
				{
					tmp.add(wr);
				}
			}
			_toReturn = tmp.toArray(new L2Object[tmp.size()]);
		}
		return _toReturn;
	}
	
	/**
	 * Return the _knownObjects containing all L2Object known by the L2Character.
	 * @return
	 */
	private final Map<Integer, L2Object> getKnownObjectsMap()
	{
		if (_knownObjects == null)
		{
			_knownObjects = new HashMap<>();
		}
		return _knownObjects;
	}
	
	public int getKnownObjectsSize()
	{
		int _toReturn = 0;
		synchronized (_lockObject)
		{
			_toReturn = getKnownObjectsMap().size();
		}
		return _toReturn;
	}
	
	public final boolean knowsObject(L2Object object)
	{
		L2Object activeObject = getActiveObject();
		if (activeObject == null)
		{
			return false;
		}
		
		if (activeObject == object)
		{
			return true;
		}
		
		boolean containsKey = false;
		synchronized (_lockObject)
		{
			containsKey = getKnownObjectsMap().containsKey(object.getObjectId());
		}
		
		return containsKey;
	}
	
	public L2Object putInKnownObjects(Integer objectId, L2Object object)
	{
		L2Object toReturn = null;
		synchronized (_lockObject)
		{
			toReturn = getKnownObjectsMap().put(objectId, object);
			if ((toReturn != null) && (toReturn instanceof L2Character))
			{
				getKnownCharactersMap().put(objectId, object);
			}
			if ((toReturn != null) && (toReturn instanceof L2NpcInstance))
			{
				getKnownNpcInstanceMap().put(objectId, object);
			}
		}
		return toReturn;
	}
	
	/** Remove all L2Object from _knownObjects */
	public void removeAllKnownObjects()
	{
		synchronized (_lockObject)
		{
			getKnownObjectsMap().clear();
		}
	}
	
	public L2Object removeInKnownObjects(Integer objectId)
	{
		L2Object toReturn = null;
		synchronized (_lockObject)
		{
			toReturn = getKnownObjectsMap().remove(objectId);
			if ((toReturn != null) && (toReturn instanceof L2Character))
			{
				getKnownCharactersMap().remove(objectId);
			}
			if ((toReturn != null) && (toReturn instanceof L2NpcInstance))
			{
				getKnownNpcInstanceMap().remove(objectId);
			}
		}
		return toReturn;
	}
	
	public boolean removeKnownObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		return (removeInKnownObjects(object.getObjectId()) != null);
	}
	
	/**
	 * Update the _knownObject and _knowPlayers of the L2Character and of its already known L2Object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove invisible and too far L2Object from _knowObject and if necessary from _knownPlayers of the L2Character</li>
	 * <li>Add visible L2Object near the L2Character to _knowObject and if necessary to _knownPlayers of the L2Character</li>
	 * <li>Add L2Character to _knowObject and if necessary to _knownPlayers of L2Object alreday known by the L2Character</li><BR>
	 * <BR>
	 */
	public final void updateKnownObjects()
	{
		if (getActiveObject() == null)
		{
			return;
		}
		
		// Only bother updating knownobjects for L2Character; don't for L2Object
		if (getActiveObject() instanceof L2Character)
		{
			synchronized (_lockObject)
			{
				findCloseObjects();
				forgetObjects();
			}
		}
	}
}
