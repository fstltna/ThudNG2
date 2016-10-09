//
//  MUCommandsTask.java
//  Thud
//
//  Created by Anthony Parker on Wed Oct 02 2002.
//  Copyright (c) 2001-2006 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.engine;

import net.sourceforge.btthud.engine.commands.HUDGeneralStatic;
import net.sourceforge.btthud.engine.commands.HUDGeneralStatus;
import net.sourceforge.btthud.engine.commands.HUDContacts;
import net.sourceforge.btthud.engine.commands.HUDContactsBuildings;
import net.sourceforge.btthud.engine.commands.HUDTactical;
import net.sourceforge.btthud.engine.commands.HUDArmorStatus;
import net.sourceforge.btthud.engine.commands.HUDWeaponStatus;
import net.sourceforge.btthud.engine.commands.HUDAmmoStatus;
import net.sourceforge.btthud.engine.commands.HUDArmorOriginal;
import net.sourceforge.btthud.engine.commands.HUDConditions;

import java.util.*;
import net.sourceforge.btthud.data.*;

public class MUCommandsTask extends TimerTask {

    MUConnectionBase	conn;
    MUData				data;
    MUPrefs				prefs;
    
    public boolean		forceContacts;
    public boolean		forceTactical;
    public boolean		forceLOS;
    public boolean		forceGeneralStatus;
    public boolean		forceArmorStatus;

    int					count;
    
    public MUCommandsTask(MUConnectionBase conn, MUData data, MUPrefs prefs)
    {
        this.conn = conn;
        this.data = data;
        this.prefs = prefs;
    }
    
    public void run()
    {
        // We've been woken up, now we need to decide which commands to send

        /* We have several options on how fast we want to send commands:
             prefs.fastCommandUpdate -> fastest: 1, 2, or 3 seconds
             prefs.mediumCommandUpdate -> 2, 5, 10 seconds
             prefs.slowCommandUpdate -> 3, 10, 15 seconds
             prefs.slugComandUpdate -> slowest: 15, 30, 45 seconds
        */
        
        try
        {
            // If we're above twice the medium update time with no data, then don't send commands, unless the data.lastDataTime is 0
            if ((System.currentTimeMillis() - data.lastDataTime) > (2000 * prefs.mediumCommandUpdate) && data.lastDataTime != 0)
            {
                if (System.currentTimeMillis() - data.lastDataTime > (2000 * prefs.slugCommandUpdate))
                {
                    // If we're over twice the slowest command, reset the timer and try again anyway
                    data.lastDataTime = System.currentTimeMillis();
                }

                // System.out.println("-> Lag: " + (System.currentTimeMillis() - data.lastDataTime));
            }
            else
            {
            	// Do we send a static general info? (See if we've changed units)
                if (data.hudStarted && conn != null && count % (4 * prefs.fastCommandUpdate) == 0)
                {
                    conn.sendCommand(new HUDGeneralStatic ());
                }
            	
            	// Do we send general status?
                if (data.hudRunning && (forceGeneralStatus || (count % (4 * prefs.fastCommandUpdate) == 0)))
                {
                    conn.sendCommand(new HUDGeneralStatus ());
                    forceGeneralStatus = false;
                }

                // Do we send a contacts?
                if (!data.myUnit.status.matches("S|s") && data.hudRunning && (forceContacts || (count % (4 * prefs.fastCommandUpdate) == 0)))
                {
                    conn.sendCommand(new HUDContacts ());
                    if (data.hiSupportsBuildingContacts())
                        conn.sendCommand(new HUDContactsBuildings ());
                    synchronized (data)
                    {
                        data.expireAllContacts();
                    }

                    forceContacts = false;
                }

                // Do we send a tactical?
                // If we know we're on an LOS-only map, send it at a faster pace
                if (data.hudRunning && (forceTactical || (count % (4 * (data.mapLOSOnly ? prefs.mediumCommandUpdate : prefs.slugCommandUpdate)) == 0)))
                {
                    conn.sendCommand(new HUDTactical (prefs.hudinfoTacHeight));
                    forceTactical = false;                    		
                }
                
                if(data.hudRunning && (data.lastLOSX != data.myUnit.position.getHexX() || 
                					   data.lastLOSY != data.myUnit.position.getHexY() || 
                					   data.lastLOSZ != data.myUnit.position.getHexZ() ||
                					   forceLOS)) {
                	// We've moved since last LOS update, request one.
                	data.clearLOS();
                    conn.sendCommand(new HUDTactical (prefs.hudinfoTacHeight, true)); //
                    data.lastLOSX = data.myUnit.position.getHexX();
                    data.lastLOSY = data.myUnit.position.getHexY();
                    data.lastLOSZ = data.myUnit.position.getHexZ();
                    forceLOS = false;
                }

                // Do we send an armor status?
                if (data.hudRunning && (forceArmorStatus || (count % (4 * prefs.mediumCommandUpdate) == 0)))
                {
                    conn.sendCommand(new HUDArmorStatus ());
                    // Also send weapon & ammo status at this time
                    conn.sendCommand(new HUDWeaponStatus ());
                    conn.sendCommand(new HUDAmmoStatus ());
                    forceArmorStatus = false;
                }
                
                // Do we send an original armor status? Only do this on startup (or if reset by auto leave/enter code)
                if(conn != null && data.lastDataTime == 0) {
                	conn.sendCommand(new HUDArmorOriginal ());
                }
                
                // Do we send a weather condition update?
                if (data.hudRunning && (count % (30 * prefs.mediumCommandUpdate) == 0))
                {
                    conn.sendCommand(new HUDConditions ());
                }
            }           
        }
        catch (Exception e)
        {
            System.out.println("Error: MUCommandsTask: " + e);
        }
        
        // Increment our count
        count++;
    }
}
