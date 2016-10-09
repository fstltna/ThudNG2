//
//  MUTacticalMap.java
//  Thud
//
//  Created by asp on Wed Nov 28 2001.
//  Copyright (c) 2001-2007 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.ui;

import net.sourceforge.btthud.ui.map.MUMapComponent;

import net.sourceforge.btthud.data.MUPrefs;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import net.sourceforge.btthud.util.*;

public class MUTacticalMap extends ChildWindow implements ActionListener, KeyListener {
	protected MUMapComponent map;

	private final Thud thud;
	protected int iMapNum;
	protected Logger myLogger;
	
	public MUMapComponent getMap ()
	{
		return map;
	}
	
	public MUTacticalMap (final Thud thud, int mapnum) {
		super (thud, "Tactical Map");
		myLogger = new Logger ("MUTacticalMap");
		
		myLogger.Log ("CONSTRUCTOR 001 :" +  mapnum + ":");
		
		this.thud = thud;
		iMapNum = mapnum;
		
		myLogger.Log ("CONSTRUCTOR 002 :" +  mapnum + ":");
		
		map = new MUMapComponent (thud.data, thud.prefs, thud, iMapNum);
		myLogger.Log ("CONSTRUCTOR 003 :" +  mapnum + ":");
		
		window.add(map);
		myLogger.Log ("CONSTRUCTOR 004 :" +  mapnum + ":");
		
		window.addKeyListener (this);
		myLogger.Log ("CONSTRUCTOR 005 :" +  mapnum + ":");
		
		if (iMapNum == 0)
		{
			myLogger.Log ("CONSTRUCTOR 006 :" +  mapnum + ":");
			
			window.setSize(thud.prefs.tacSizeX, thud.prefs.tacSizeY);
			window.setLocation(thud.prefs.tacLoc);
		}
		else
		{
			myLogger.Log ("CONSTRUCTOR 007 :" +  mapnum + ":");
			
			window.setSize(thud.prefs.tac2SizeX, thud.prefs.tac2SizeY);
			window.setLocation(thud.prefs.tac2Loc);
		}
		
		myLogger.Log ("CONSTRUCTOR 008 :" +  mapnum + ":");
		window.setAlwaysOnTop(thud.prefs.tacticalAlwaysOnTop);
		myLogger.Log ("CONSTRUCTOR 009 :" +  mapnum + ":");
		
		// Show the window now
		window.setVisible(true);
		
		myLogger.Log ("CONSTRUCTOR OUT :" +  mapnum + ":");
	}

	public void newPreferences (final MUPrefs prefs) {
		super.newPreferences(prefs);
		window.setAlwaysOnTop(prefs.tacticalAlwaysOnTop);
		map.newPreferences(prefs);
	}

	public void actionPerformed (final ActionEvent ae) {
		// TODO: Make MUMapComponent only access MUData at this
		// well-defined point.  This will let us consolidate all the
		// refresh() procedures.
		// TODO: Ensure this is only called with sync at thud.data.
		map.refresh(thud.data);
	}
	
    public void keyPressed (KeyEvent ke)
    {
    	
    }
    
    public void keyTyped (KeyEvent ke)
    {
    	
    }
    
    public void keyReleased (KeyEvent ke)
    {
        if (map.prefs.bStopMapEvents)
        	return;

        map.mapActions.fireKeyEvent (map.myThud, map, ke);
        
//        if (false)
//        {
//    	char myKey = ke.getKeyChar ();
//    	
//    	System.out.println ("keyTyped: 001");
//    	
//    	int		modifiers = ke.getModifiersEx ();
//    	
//    	System.out.println ("KY:Checking Modifiers");
//    	if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK)
//    		System.out.println ("KY:SHIFT IS DOWN");
//    	else
//    		System.out.println ("KY:SHIFT IS UP");
//
//    	if ((modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)
//    		System.out.println ("KY:CTRL IS DOWN");
//    	else
//    		System.out.println ("KY:CTRL IS UP");
//
//    	if ((modifiers & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK)
//    		System.out.println ("KY:ALT IS DOWN");
//    	else
//    		System.out.println ("KY:ALT IS UP");
//    	
//    	if (myKey == '0' ||
//    		myKey == '1' ||
//    		myKey == '2' ||
//    		myKey == '3' ||
//    		myKey == '4' ||
//    		myKey == '5' ||
//    		myKey == '6' ||
//    		myKey == '7' ||
//    		myKey == '8' ||
//    		myKey == '9')
//    	{
//        	System.out.println ("keyTyped: 002 :" + myKey + ":");
//        	
//    		char [] myChars = new char [6];
//    		
//    		myChars [0] = 'f';
//    		myChars [1] = 'i';
//    		myChars [2] = 'r';
//    		myChars [3] = 'e';
//    		myChars [4] = ' ';
//    		myChars [5] = myKey;
//    		
//    		String myCmd = new String (myChars);
//    		
//        	System.out.println ("keyTyped: 003 :" + myCmd + ":");
//        	
//			try {
//				thud.getConn().sendCommand(new UserCommand (myCmd));
//			} catch (Exception e1) {
//				// TODO: Seems like it'd be more friendly to report
//				// these errors in the main window, or in a modal
//				// dialog.  Hiding things in the console is so like
//				// 1990.
//				System.err.println("Can't send: " + e1);
//			}
//    	}
//        }
    }
}
