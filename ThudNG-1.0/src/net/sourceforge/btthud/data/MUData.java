//
//  MUData.java
//  Thud
//
//  Created by asp on Tue Nov 20 2001.
//  Copyright (c) 2001-2006 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.data;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.awt.event.ActionListener;
import net.sourceforge.btthud.util.*;


/**
 * Stores all the information from contacts and tactical.
 *
 * Some notes:
 *   Since a lot of other classes (mainly ones for displaying info) use this, we should keep things
 *  thread safe if possible. We want to store most (if not all) of the info in this one class so that
 * 	we can keep things simple when passing data around.
 *
 * @author Anthony Parker
 */


public class MUData implements Runnable {

    // Making these public sorta defeats the purpose of hiding them in the class in the first place, but
    // I just want to make it easier on myself at this point. Maybe I'll fix it later.

    public static final int		MAX_X = 1000;
    public static final int		MAX_Y = 1000;
    
    public boolean				hudRunning = false, hudStarted = false;
    public boolean				mainWindowMuted = false;
    
    public MUMyInfo				myUnit = null;

    // The map
    MUHex						map[][] = null;
    
    final int                   iNumMaps = 2;
    
    boolean	[]					terrainChanged;
    
    public String				mapName, mapId, mapVersion;    
    public boolean				mapLOSOnly = false;
    public String				mapFileName;

    // One MUHex for each elevation and terrain
    // By storing references to MUHexes we can save memory
    // 19 = -9 thru 0 and 1 thru 9
    MUHex						hexCache[][] = new MUHex[MUHex.TOTAL_TERRAIN][19];
    
    // We store the contact data in a ArrayList, because we need to iterate over it efficiently
    public ArrayList<MUUnitInfo>		contacts = null;
    ArrayList<MUUnitInfo>		buildings = null;
    
    // LOS info is in a Hashtable since we don't iterate, we query it
    public Hashtable			LOSinfo = new Hashtable();	  
    public int					lastLOSX = 0, lastLOSY = 0, lastLOSZ = 0;

    // Weather info
    public MUWeather			weather;
    
    // This is the time that we received our last hudinfo data
    public long					lastDataTime;

    // What version of hudinfo are we working with?
    int						hudInfoMajorVersion = 0;
    int						hudInfoMinorVersion = 0;
    
    protected Logger		myLogger;
    
    /**
     * Constructor
     */
    public MUData()
    {
        hudRunning = false;
        myLogger = new Logger ("MUData");
        
        terrainChanged = new boolean [iNumMaps];
        
        int		i;
        
        for (i = 0; i < iNumMaps; ++i)
        	terrainChanged [i] = true;
        
        clearData();

        createHexCache();
        
        LOSinfo = new Hashtable();
        
        map = new MUHex[MAX_X][MAX_Y];		// individual hexes will be allocated if they are needed.. this is not very memory efficient still

	start();
    }
        
    public void createHexCache()
    {
        for (int i = 0; i < MUHex.TOTAL_TERRAIN; i++)
        {
            for (int j = -9; j < 10; j++)
            {
                hexCache[i][j + 9] = new MUHex(i, j);
            }
        }
    }
    
    /**
      * Adds a new contact to our list of contacts, or updates an existing one.
      * 
      * @param con		Contact to be added
      */
    public void newContact(MUUnitInfo con)
    {
        int			index = indexForId(con.id);

        myLogger.Log ("NewContact: 001");
        
        if (index != -1 && con.bTempContact)
        {
            myLogger.Log ("NewContact: 002");
            
        	MUUnitInfo xUnit = contacts.get(index);
        	
        	myLogger.Log ("NewContact: 003");
        	
        	/* Do not replace a real contact for a temporary contact */
        	if (!xUnit.bTempContact)
        	{
        		myLogger.Log ("NewContact: 004");
        		return;
        	}
        }
        
        /* dont list hexmes of me */
        if (index == -1 && con.id.toLowerCase ().equals(myUnit.id.toLowerCase ()))
        	return;
        
        myLogger.Log ("NewContact: 005");
        
        if (index != -1)
        {
        	myLogger.Log ("NewContact: 006");
            contacts.set(index, con);
        }
        else
        {
        	myLogger.Log ("NewContact: 007");
            contacts.add(con);
        }
        
        myLogger.Log ("NewContact: OUT");
    }

    /**
      * Iterates through the contact list and marks contacts as expired or old or whatever.
      */
    public void expireAllContacts()
    {
        try
        {
            // We need a ListIterator because it allows us to modify the list while we iterate
            ListIterator		it = contacts.listIterator();

            while (it.hasNext())
            {
                MUUnitInfo		unit = (MUUnitInfo) it.next();

                if (unit.isExpired())
                    it.remove();					// Remove this contact		
                else
                    unit.expireMore();				// Increase the age of this contact
            }
        }
        catch (Exception e)
        {
            System.out.println("Error: expireAllContacts: " + e);
        }            
    }

    /**
      * Returns the index in the contacts ArayList of a specific map id.
      * Since we keep track of everything in our LinkedList in our hashtable, just check the hashtable to see if we have it
      * 
      * @param	id		Map ID of contact to find
      */
    protected int indexForId(String id)
    {
        ListIterator		it = contacts.listIterator();
        int					index;
        
        while (it.hasNext())
        {
            index = it.nextIndex();

            // See if the next unit's upper-case id matches the id sent in
            if (((MUUnitInfo) it.next()).id.toUpperCase().equals(id.toUpperCase()))
                return index;            
        }

        // Must not have found it
        return -1;
    }

    /**
     * Returns a MUUnitInfo of a specific map id.
     * @param	id		Map ID of unit to return
     */
    public MUUnitInfo getContact(String id) {
    	return (MUUnitInfo) contacts.get(indexForId(id));    	
    }
    
    /**
      * Returns an Iterator for the contact list. Used for looping on contacts when drawing the map, for example
      * @param sorted True if we want a sorted list
      */
    public Iterator getContactsIterator(boolean sorted)
    {
        if (!sorted)
            return contacts.iterator();
        else
            return ((new TreeSet<MUUnitInfo>(contacts)).iterator());
    }
    
    // ----------------------------------

    /**
      * Get the terrain of a specific hex (return the id, not the char).
      * @param x X coordinate
      * @param y Y coordinate
      */
    public int getHexTerrain(int x, int y)
    {        
        if (x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y)
        {
            if (map[x][y] != null) {
            	if(map[x][y].hasDS) {
            		return MUHex.WALL;
            	} else {
            		return map[x][y].terrain();
            	}
            }
            else
                return MUHex.UNKNOWN;
        }

        return MUHex.UNKNOWN;
    }

    /**
     * Get the elevation of a specific hex.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public int getHexElevation(int x, int y)
    {
        if (x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y)
        {
            if (map[x][y] != null)
                return map[x][y].elevation();
            else
                return 0;
        }

        return 0;
    }

    /**
     * Get the "cliff" elevation of a specific hex (is negative for water, and 0 for ice).
     * @param x X coordinate
     * @param y Y coordinate
     */
    public int getHexCliffElevation(int x, int y)
    {
        int	e = getHexElevation(x, y);
        // Since we use this function in determining cliff edges, a few corrections...

        if (getHexTerrain(x, y) == MUHex.ICE)		// ice
            e = 0;									// You can cross it, even tho it may be dangerous
        
        return e;
    }

    /**
     * Set the details of a specific hex.
     * @param x X coordinate
     * @param y Y coordinate
     * @param ter The terrain character representation
     * @param elevation The elevation of the hex
     */
    public void setHex(int x, int y, char ter, int elevation)
    {
        if (x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y)
        {
            map[x][y] = hexCache[MUHex.idForTerrain(ter)][elevation + 9];
        }
    }
    
  /**
	 * Change a normal terrain hex into a hex with a dropship marker on it. Used
	 * by map drawing stuff to draw a '=' instead of normal terrain.
	 */
    public void setHexDS(int x, int y) {
		if (x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y) {
			map[x][y] = new MUHex(getHexTerrain(x, y), getHexElevation(x, y));
			map[x][y].hasDS = true;
		}
	}
    
    /**
	 * Clear dropship marker from a hex.
	 */
      public void setHexNoDS(int x, int y) {
		if (x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y) {
			map[x][y] = hexCache[map[x][y].terrain()][getHexElevation(x,y) + 9];
		}
	}
    
  /**
    * Clear data that is 'Mech specific, so that when we start the HUD again we have a clean slate.
    */
    public void clearData()
    {
        // Clear contacts and our unit, but leave the map alone
        contacts = new ArrayList<MUUnitInfo>(20);		// data for our contact list
        myUnit = new MUMyInfo();			// data that represents our own unit
        clearMap();
        this.mapName = "";
        weather = new MUWeather();
        lastDataTime = 0;					// clear our last recieved data
        clearLOS();
    }
    
    /**
     * Re-initializes map.
     */
    public void clearMap()
    {
    	map = new MUHex[MAX_X][MAX_Y];
    }
    
    /**
     * Clear LOS data.
     */
    public void clearLOS()
    {
    	LOSinfo = new Hashtable();
    }
    /**
      * Sets the map changed flag.
      */
    public void setTerrainChanged(boolean b, int idx)
    {
    	if (idx < 0 || idx >= iNumMaps)
    		return;
    	
        terrainChanged [idx] = b;
    }

    public void setAllTerrainChanged (boolean b)
    {
    	int i;
    	
    	for (i = 0; i < iNumMaps; ++i)
    		terrainChanged [i] = b;
    }
    
    /**
      * Has the terrain changed?
      */
    public boolean terrainChanged(int idx)
    {
    	if (idx < 0 || idx >= iNumMaps)
    		return false;
    	
        return terrainChanged [idx];
    }

    public void setHudInfoMajorVersion(int v)
    {
        hudInfoMajorVersion = v;
    }

    public void setHudInfoMinorVersion(int v)
    {
        hudInfoMinorVersion = v;
    }

    // ---------------------------------
    // These functions are to determine if certain features exist (they were added in particular versions)
    // I thought it'd be easier to keep track of these here instead of spreading them across multiple files
    // that have 'magic numbers' to compare to
    // 'hi' = hudinfo

    public boolean hiSupportsOwnJumpInfo()
    {
        if (hudInfoMinorVersion > 6)
            return true;
        else
            return false;
    }

    public boolean hiSupportsWLHeatInfo()
    {
        if (hudInfoMinorVersion > 6)
            return true;
        else
            return false;
    }

    public boolean hiSupportsBuildingContacts()
    {
        if (hudInfoMinorVersion > 6)
            return true;
        else
            return false;
    }

    public boolean hiSupportsExtendedMapInfo()
    {
        if (hudInfoMinorVersion > 6)
            return true;
        else
            return false;
    }

    public boolean hiSupportsAllArgumentHudinfo()
    {
        if (hudInfoMinorVersion > 6)
            return true;
        else
            return false;
    }
    
    /** Attempts to load map information from a .tmap file on disk.
     * Uses the name of the current map . tmap (ie: 'DC.city3.tmap')
     * @return true if succesful, false if error/no file found/etc
     */
    public boolean loadMapFromDisk() {
    	if(mapFileName.length() <= 1) // do we have a real mapname?
    		return false;
    	try {
    		    	
    		File mapFile = new File(mapFileName);
    		
    		BufferedReader brin = new BufferedReader(new FileReader(mapFile));
    		String s = brin.readLine();
    		
    		/* Check and see if this is a mux-format mapfile */
    		String patternStr = "^([0-9]+) ([0-9]+$)";
    		Pattern pattern = Pattern.compile(patternStr);
    		Matcher matcher = pattern.matcher(s);
    		boolean matchFound = matcher.find();
    		
    		if(matchFound) {
    			/* Looks like a btech format, let's parse it. */
    			int mapMaxX = Integer.parseInt(matcher.group(1));
    			int mapMaxY = Integer.parseInt(matcher.group(2));
				System.out.println("Reading btmap file: " + String.valueOf(mapMaxX) + " by " + String.valueOf(mapMaxY));
				map = new MUHex[MAX_X][MAX_Y];
				int x = 0;			
				int y = 0;				
    			while ((s = brin.readLine()) != null) {
    				if(!s.matches("(\\D\\d)+")) { // only process line if it looks like good terrain
    					System.out.println("Rejecting line: " + s);
    				} else { // good line    				
	    				while(x < mapMaxX) {
	    					CharSequence hexSeq = s.subSequence(x * 2, (x * 2) + 2);
	    					char terrain = hexSeq.charAt(0);
	    					char elev = hexSeq.charAt(1);
	    					//System.out.println("Hex " + x + " " + y + "= " + terrain + " level " + elev);
	    					map[x][y] = hexCache[MUHex.idForTerrain(terrain)][Integer.parseInt(String.valueOf(elev)) + 9];
	    					x++;
	    				}
	    				x=0;
	    				y++;
    				}
    			}   
    			return true;
    		} else {
    			/* Not a btech format mapfile, try using our 'Thud Format' */
        		FileInputStream in = new FileInputStream(mapFile);
        		ObjectInputStream ois = new ObjectInputStream(in);
        		map = new MUHex[MAX_X][MAX_Y];
        		map = (MUHex[][]) ois.readObject();
        		ois.close();
        		in.close();                 
        		
        		return true;
    		}
    	}
    	catch(Exception e) {
    		System.out.println("Error loading map " + mapFileName + ": " + e);
    		return false;
    	}    
    }
    
    /** Attempts to write map information to a .tmap file on disk.
     * Uses the name of the current map . tmap (ie: 'DC.city3.tmap')
     * @return true if succesful, false if error/file io error/etc
     */
    public boolean saveMapToDisk() {
    	if(mapFileName == null || mapFileName.length() <= 1) // do we have a real mapname?
    		return false;
    	try {    		
    		File mapFile = new File(mapFileName);
    		mapFile.delete();
    		mapFile.createNewFile();
    		FileOutputStream out = new FileOutputStream(mapFile);
    		ObjectOutputStream oos = new ObjectOutputStream(out);
    		oos.writeObject(this.map);
    		oos.flush();
    		
    		oos.close();
    		out.close();
    		
    		return true;
    	} 
    	catch(Exception e) {
    		System.out.println("Error saving map " + mapName + ".tmap: " + e);
    		return false;
    	}    
    }


    /**
     * Validate state.  Users should validate before using certain kinds of
     * data.  In particular, this handles dropships, which modify the terrain
     * they land on.
     */
    public void validate () {
        // Clear terrain around landed dropships.
        Iterator contacts = getContactsIterator(false);

        while (contacts.hasNext()) {
            final MUUnitInfo unit = (MUUnitInfo)contacts.next();

            if (unit.type.equals("D") || unit.type.equals("A")) {
                final int unitX = unit.getX();
                final int unitY = unit.getY();

                if (unit.getZ() == getHexElevation(unitX, unitY)) {
                    // Landed dropship.
                    setHexDS(unitX, unitY);
                    setHexDS(unitX - 1, unitY - 1);
                    setHexDS(unitX, unitY - 1);
                    setHexDS(unitX + 1, unitY - 1);
                    setHexDS(unitX - 1, unitY);
                    setHexDS(unitX + 1, unitY);
                    setHexDS(unitX, unitY + 1);

                    setAllTerrainChanged(true);
                }
            }
        }
    }


	//
	// Data update thread.
	//

	private final List<ActionListener> listenerList = new ArrayList<ActionListener> ();

	public void addActionListener (final ActionListener listener) {
		listenerList.add(listener);
	}

	private void runActionListeners () {
		for (final ActionListener listener: listenerList) {
			listener.actionPerformed(null);
		}
	}


	private boolean go = true;

	private void start () {
		new Thread (this, "MUData").start();
	}

	public void run () {
		synchronized (this) {
			while (go) {
				runActionListeners();

				try {
					this.wait(1000);
				} catch (final InterruptedException e) {
					// No big deal.
				}
			}
		}
	}

	public void pleaseStop () {
		synchronized (this) {
			go = false;
			this.notify();
		}
	}
}
