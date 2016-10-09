//
//  MUMapComponent.java
//  Thud
//
//  Created by Anthony Parker on Thu Dec 27 2001.
//  Copyright (c) 2001-2007 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.ui.map;

import net.sourceforge.btthud.data.*;
import net.sourceforge.btthud.ui.*;
import net.sourceforge.btthud.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;

import javax.swing.*;
import java.util.*;

import java.awt.geom.Ellipse2D;

/* Notes:

This is the class that does probably the most interesting work in all of Thud. It draws the map and the components inside the map.

It tries to draw as much as possible beforehand (in the changeHeight() function) so as to do as little processing as possible when it is drawing a lot of hexes across and down. This includes filling, drawing lines, drawing terrain types, and drawing elevations. It stores all of this in an array then copies it while running. It is probably possible to do further optimizations here, and that would be a good idea.

Also needed; a better way to determine exactly what hexes to draw or not. Right now it just guestimates then adds on some on both sides. We could avoid drawing a lot of hexes if we had a more accurate algorithm.

- asp, 7/7/2002

    */

public class MUMapComponent extends JComponent implements ComponentListener
{
	private static final long serialVersionUID = 1;
	
	public Thud				myThud;
	public MUData			data;
	public MUPrefs			prefs;
	
	protected				int		iMapNum;
	
    Font					tacStatusFont;
    Font					hexNumberFont;
    Font					infoFont;
    Font					terrainFont;
    Font					elevFont;

    double					PI = Math.PI;

    FontRenderContext		frc;
    
    int						numAcross = 20;
    int						numDown = 20;

    static final int		HEADING_NORMAL = 0;
    static final int		HEADING_DESIRED = 1;
    static final int		HEADING_JUMP = 2;
    static final int		HEADING_TURRET = 3;
    
    int						elevWidth[] = new int[10]; 	// Stores width of each elevation number glyph, 0 - 9
    
    BufferedImage			hexImages[][][] = new BufferedImage[MUHex.TOTAL_TERRAIN][10][2];			// One for each hex type, elevation, and LOS or not

    GeneralPath				gp = new GeneralPath();
    HexShape				hexPoly;

    int						h = 40;
    float					w = h * 2f * MUConstants.ALPHA;
    float					l = h *      MUConstants.ALPHA;

    int						barHeight = 15;
    int						heatBarMaxLength = 50;

    RenderingHints			rHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Rectangle				bounds;
    BufferedImage			savedTerrain = null;
    Point2D					savedTerrainCenter = new Point2D.Float(0,0);
    Rectangle				savedTerrainBounds = new Rectangle(0, 0, 0, 0);
    
    boolean					bPainting = false;
    
    public MUMapActions 	mapActions;
    
    Logger					myLogger;
    
    public int getMapNum ()
    {
    	return iMapNum;
    }
    
    public boolean isXPainting() {
		return bPainting;
	}

	public void setXPainting(boolean painting) {
		bPainting = painting;
	}

	public MUMapComponent(MUData data, MUPrefs prefs, Thud thud, int mapnum)
    {
        super();
        myLogger = new Logger ("MUMap");
        
        myLogger.Log("CONSTSRUCTOR: 001 :" + mapnum + ":");
        
        iMapNum = mapnum;
        
        myLogger.Log("CONSTSRUCTOR: 002 :" + mapnum + ":");
        
        this.data = data;
        this.prefs = prefs;

        myThud = thud;
        
        setupFonts();

        setDoubleBuffered(true);

        MapMouseListener myMapListener = new MapMouseListener (this);
        
        addMouseListener(myMapListener);
        addMouseWheelListener (myMapListener);
        
        addComponentListener(this);
        
        myLogger.Log("CONSTSRUCTOR: 003 :" + mapnum + ":");
        
        // Do some initial setup
        if (iMapNum == 1)
        	changeHeight(prefs.hexHeight2);
        else	
        	changeHeight(prefs.hexHeight);
 
        myLogger.Log("CONSTSRUCTOR: 004 :" + mapnum + ":");

        precalculateNumbers();

        myLogger.Log("CONSTSRUCTOR: 005 :" + mapnum + ":");

        bounds = getBounds();
        
        //rHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        rHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                   prefs.antiAliasText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        
        myLogger.Log("CONSTSRUCTOR: 006 :" + mapnum + ":");

		mapActions = thud.mapActions;
		
        myLogger.Log("CONSTSRUCTOR: OUT :" + mapnum + ":");
    }
    
    // --------------
    
    public void componentHidden(ComponentEvent e)
    {
        
    }

    public void componentMoved(ComponentEvent e)
    {

    }

    public void componentResized(ComponentEvent e)
    {
        bounds = getBounds();

        newPreferences(prefs);
    }

    public void componentShown(ComponentEvent e)
    {

    }
    
    /* ---------------------- */

    /**
      * Creates a new BufferedImage for the terrain.
      */

    protected void createNewSavedTerrain()
    {
        int		newWidth = (int) (bounds.width * 2);
        int		newHeight = (int) (bounds.height * 2);

        // We only need to allocate this massive thing when the bounds have actually changed
        if (savedTerrainBounds.getWidth() != newWidth || savedTerrainBounds.getHeight() != newHeight)
        {
            savedTerrain = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            savedTerrainBounds = new Rectangle(0, 0, newWidth, newHeight);            
        }
        
        savedTerrainCenter = realForUnit(data.myUnit);

        data.setTerrainChanged(true, iMapNum);
    }
    
    /**
      * The purpose of this function is to take some CPU-intensive stuff that mostly stays the same and move it outside
      * of the main drawing loops, for efficiency. We store the values in private class variables. If any font sizes change,
      * this function should be called again.
      */
    private void precalculateNumbers()
    {
        frc = new FontRenderContext(new AffineTransform(), true, prefs.antiAliasText);
        			//((Graphics2D) getGraphics()).getFontRenderContext();
        
        Rectangle2D	elevRect;
        
        for (int i = 0; i < 10; i++)
        {
            elevRect = elevFont.getStringBounds(Integer.toString(i), frc);
            elevWidth[i] = (int) elevRect.getWidth();
        }
    }

    /**
      * This function is for outside callers who want to make sure that we redraw in case of preference color or font change.
      */
    public void newPreferences(MUPrefs prefs)
    {
        this.prefs = prefs;

        setupFonts();
        if (iMapNum == 1)
        	changeHeight(prefs.hexHeight2);
        else
        	changeHeight(prefs.hexHeight);
        
        createNewSavedTerrain();
        
        repaint();
    }

	public void refresh (final MUData data) {
		if (!data.hudRunning)
			return;

		repaint();
	}

    /**
      * The purpose of this function is to take some CPU-intensive stuff that mostly stays the same and move it outside
      * of the main drawing loops, for efficiency. This one deals with saving the Images for each color of hex, to avoid 'fill' costs.
      * It should be called any time that the height changes.
      */
    private void changeHeight(int newHeight)
    {
    	//myLogger.Log("changeHeight: 001 :" + newHeight + ":");
    	
        // Set some variables
        if (newHeight < 5)
            h = 5;
        else
            h = newHeight;

    	//myLogger.Log("changeHeight: 002 :" + newHeight + ":");

        w = h * 2f * MUConstants.ALPHA;
        l = h *      MUConstants.ALPHA;

        terrainFont = new Font("Monospaced", Font.PLAIN, h/2 - 2);

        hexPoly = new HexShape(h);
        
    	//myLogger.Log("changeHeight: 003 :" + MUHex.TOTAL_TERRAIN + ":");

        // Now draw our images      
        for (int i = 0; i < MUHex.TOTAL_TERRAIN; i++)
        {
        	//myLogger.Log("changeHeight: 004 :" + i + ":");

            for (int j = 0; j < 10; j++)
            {
            	//myLogger.Log("changeHeight: 005 :" + j + ":");

            	for (int k = 0; k < 2; k++) {
            		//myLogger.Log("changeHeight: 006 :" + k + ":");
 
            		boolean darkHex = false; // used for determining white or black writing
            		
	                BufferedImage	newImage = new BufferedImage(hexPoly.getBounds().width, hexPoly.getBounds().height, BufferedImage.TYPE_INT_ARGB);
	
	                // Get the graphics context for this BufferedImage
	                Graphics2D		g = (Graphics2D) newImage.getGraphics();
	
	                // Setup the color
	                if (prefs.tacDarkenElev)
	                    g.setColor(MUHex.colorForElevation(colorForTerrain(i), j, prefs.elevationColorMultiplier));
	                else
	                    g.setColor(colorForTerrain(i));
	                
	                Color terrainColor = g.getColor();
	                // Dark-colored hexes get white text
	                if(terrainColor.getRed() < 128 && terrainColor.getGreen() < 128 && terrainColor.getBlue() < 128)
	                	darkHex = true;
	                // So do all water hexes
	                if(i == MUHex.WATER)
	                	darkHex = true;
	                
	                // Fill the hex
	                g.fill(hexPoly);
	
	                // Draw the line around the hex
	                g.setColor(Color.gray);
	                g.draw(hexPoly);                    
	                
	                // Draw the elevation number (lower right corner)
	                if (prefs.tacShowTerrainElev && h >= 20)
	                {
	                    // Draw the elevation
	                	if(darkHex) {
	                		g.setColor(Color.white);
	                	} else {
	                		g.setColor(Color.black);
	                	}
	                    g.setFont(elevFont);
	
	                    if (j != 0)			// We don't draw zero elevation numbers
	                    {
	                        String		hexElev = Integer.toString(j);
	                        int			width;
	
	                        if (j < 0 && Math.abs(j) <= 9)
	                            width = 2 * elevWidth[Math.abs(j)];
	                        else if (j > 0 && j <= 9)
	                            width = elevWidth[j];
	                        else
	                            width = elevWidth[0];
	
	                        g.drawString(hexElev,
	                                     (float) (hexPoly.getX(0) + w - width),
	                                     (float) (hexPoly.getY(0) + h - 2));
	                    }
	                }
	
	                // Draw the terrain type (upper left corner)
	                if (prefs.tacShowTerrainChar && h >= 20)
	                {
	                	String terrain = String.valueOf(MUHex.terrainForId(i));
	                    if (i == MUHex.PLAIN)			// we don't draw plain types
	                    	terrain = " ";
	                    if(k == 0) // LOS-less hex 
	                    	terrain += "?";
	                    
                        g.setFont(terrainFont);
                        g.drawString(terrain, (float) hexPoly.getX(0), (float) (hexPoly.getY(0) + h/2));
	                    
	                }
	                
	                // Shade for no-LOS
	                if (k == 0) {
	                	if(i == MUHex.LIGHT_FOREST || i == MUHex.HEAVY_FOREST) {
	                		g.setColor(new Color(0, 0, 255, 64)); // forests get blue
	                	} else {
		                	g.setColor(new Color(0, 255, 255, 64)); // most hexes get cyan
	                	}
	                	g.fill(hexPoly);
	                }
	                hexImages[i][j][k] = newImage;
            	}
            }
        }
        
       	//myLogger.Log("changeHeight: OUT");
    }
    
    /**
      * This function sets up the fonts, according to the sizes specified in the preferences. It should be called each time the font size
      * changes.
      */
    private void setupFonts()
    {
        tacStatusFont = new Font("Monospaced", Font.PLAIN, prefs.tacStatusFontSize);
        hexNumberFont = new Font("Monospaced", Font.BOLD, prefs.hexNumberFontSize);
        infoFont = new Font("Monospaced", Font.BOLD, prefs.infoFontSize);
        terrainFont = new Font("Monospaced", Font.PLAIN, 10);		// this changes dynamically anyway. based on size of hex
        elevFont = new Font("Monospaced", Font.PLAIN, prefs.elevationFontSize);

        rHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                   prefs.antiAliasText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        
        // need to recalculate font widths
        precalculateNumbers();
    }
    
    /* ---------------------- */

    /**
     * Paints the whole map, in steps.
     */
    public void paint(Graphics gfx)
    {        
    	bPainting = true;
    	
        getBounds(bounds);

        if (savedTerrain == null)
            createNewSavedTerrain();
        
        Graphics2D			g = (Graphics2D) gfx;
        AffineTransform		oldTrans = g.getTransform();

        g.addRenderingHints(rHints);

        if (iMapNum == 1)
        {
	        if (prefs.hexHeight2 != h)
	            changeHeight(prefs.hexHeight2);
        }
        else
        {
	        if (prefs.hexHeight != h)
	            changeHeight(prefs.hexHeight);
        }
        
        // First, let's do some initial setup on our tactical map area
        g.setColor(Color.blue);
        g.fill(bounds);

        // How many hexes to draw?
        numAcross = (int) ((bounds.width / (w + l)) + 3) * 2;
        numDown = (int) ((bounds.height / h) + 3) * 2;

        // Keep numAcross and numDown even
        if (numAcross % 2 != 0)
            numAcross++;

        if (numDown % 2 != 0)
            numDown++;
        
        // ----

        synchronized (data)
        {
            // Paint the terrain
            paintTerrain(g);

            // Paint contacts on the map, including our own unit
            paintContacts(g);

            // Paint hex numbers
            paintNumbers(g);

            // Draw our status bar at the bottom of the screen
            paintStatusBar(g);
            
            // Draw armor diagram
            if(data.hudRunning && prefs.tacShowArmorDiagram) {
            	paintArmorDiagram(g);
            }
            
        }

        // ----
        
        // Reset the transform
        g.setTransform(oldTrans);
        
    	bPainting = false;
    }

    /**
     * Paint our own unit on the map.
     * We should draw at the exact center. paintTerrain() should have translated the hexes underneath us to show the correct location.
     * @param g The graphics context into which we are drawing.
     */
    public void paintUnit(Graphics2D g)
    {
        Point2D	 		unitPt = new Point2D.Double();

        // Get the location of our own unit
        unitPt = realForUnit(data.myUnit);

        myLogger.Log("paintUnit: 01 :" + data.myUnit.id + ": (" + new Double (data.myUnit.position.getX ()).toString () + "," + new Double (data.myUnit.position.getY ()).toString () + ")");
        myLogger.Log("paintUnit: 02 :" + data.myUnit.id + ": (" + new Double (unitPt.getX ()).toString () + "," + new Double (unitPt.getY ()).toString () + ")");
        myLogger.Log("unitLoc (" +
            	data.myUnit.getX () + "," + data.myUnit.getY () + ")");

        if (prefs.highlightMyHex)
        {
            Stroke				saveStroke = g.getStroke();
            AffineTransform		saveTrans = g.getTransform();
            AffineTransform		newTrans = new AffineTransform(saveTrans);

            final Point2D hexPt = getHexToReal(data.myUnit.position);
            
            g.setStroke(new BasicStroke(2.0f));
            newTrans.translate(hexPt.getX() - l, hexPt.getY());
            g.setTransform(newTrans);
            g.setColor(Color.black);
            g.draw(hexPoly);

            g.setTransform(saveTrans);
            g.setStroke(saveStroke);
        }
        
        // Draw it
        drawHeading(g, unitPt, data.myUnit, HEADING_NORMAL);
        if (data.myUnit.heading != data.myUnit.desiredHeading)
            drawHeading(g, unitPt, data.myUnit, HEADING_DESIRED);
        if (data.myUnit.isTank())
            drawHeading(g, unitPt, data.myUnit, HEADING_TURRET);
        // Limitations in hudinfo mean we don't know our own jump heading... when hudinfo is updated and MUParse is updated we can draw this
        //if (data.myUnit.isJumping())
        //    drawHeading(g, unitPt, data.myUnit, HEADING_JUMP);
        
        drawIDBox(g, data.myUnit, unitPt, true);
    }
    
    public void drawTarget (Graphics2D g, Point2D conPoint, MUUnitInfo unit)
    {
    	if (unit.isTarget ())
    	{
//    		System.out.println ("drawTarget");
//    		System.out.println ("HEX SIZE (" + new java.lang.Double ((double)w).toString () +
//    			"," + new Integer (h).toString () + ")");
    		
            AffineTransform		oldTrans = g.getTransform();
            Stroke				oldStroke = g.getStroke();
//            float				borderWidth = 4f;
//            float				borderHeight = 4f;
//            float				spacing = 5f;		// Distance between icon and name

            // Setup this transform

            AffineTransform		contactMove = new AffineTransform(oldTrans);
            contactMove.translate(conPoint.getX() - h/4f, conPoint.getY() - h/4f);
            g.setTransform(contactMove);

            g.setColor(Color.red);			// or yellow

//            Rectangle2D	iconBounds = new Rectangle2D.Float(0, 0, h/2, h/2);
//            g.setStroke(new BasicStroke(1));

            Ellipse2D.Float myEllipse = new Ellipse2D.Float (0, 0, h/2, h/2);
            g.draw(myEllipse);
            
            Line2D.Float myLine = new Line2D.Float (h/4, 0, h/4, h/2);
            g.draw (myLine);
            
            myLine = new Line2D.Float (0, h/4, h/2, h/4);
            g.draw (myLine);
            
            g.setStroke(oldStroke);
            g.setTransform(oldTrans);
    	}
    }
    
    /**
     * Paint other contacts on the map (not own unit)
     * @param g The graphics context into which we are drawing.
     */
    public void paintContacts(Graphics2D g)
    {
        AffineTransform			oldTrans = g.getTransform();

        MUUnitInfo				unit;
        Point2D					conPoint = new Point2D.Float();
        Iterator				contacts = data.getContactsIterator(false);		// we don't care if it's sorted here
        
        //System.out.println ("paintContacts: 001");
        
        // Set the transform that translates everything so as to make our own unit the center of the display
        if (prefs.xOffset != 0 || prefs.yOffset != 0)
        {
            AffineTransform			xform = new AffineTransform(oldTrans);
            xform.translate(-prefs.xOffset * (w + l), -prefs.yOffset * h);
            g.setTransform(setupStandardTransform(xform, bounds));
        }
        else
        {
            g.setTransform(setupStandardTransform(oldTrans, bounds));
        }

        // Paint our own unit first
        paintUnit(g);
 
        //System.out.println ("paintContacts: 002");
        
        // We could sort these by range, so closer units always stay on top... or something
        // But really, who cares
        while (contacts.hasNext())
        {
            // Get the next unit...
            unit = (MUUnitInfo) contacts.next();

            myLogger.Log ("paintContacts: 003 unit " + unit.id);
            myLogger.Log ("paintContacts: 004 target :" + new Boolean (unit.isTarget ()).toString ());
            
            // Figure out where it is supposed to be drawn
            conPoint = realForUnit(unit);

            myLogger.Log ("paintContacts: 004.01 (" + new Double (conPoint.getX ()).toString () + "," + new Double (conPoint.getY ()).toString () + ")");
            myLogger.Log ("paintContacts: 004.02 (" + new Double (unit.getX ()).toString () + "," + new Double (unit.getY ()).toString () + ")");

            // Draw it
            drawHeading(g, conPoint, unit, HEADING_NORMAL);
            if (unit.isJumping())
                drawHeading(g, conPoint, unit, HEADING_JUMP);
            // Limitations in hudinfo keep us from knowing the turret heading of enemy contacts, or we could draw that as well

            // Draw box for contact ID
            drawIDBox(g, unit, conPoint, false);
            
            if (unit.isTarget ())
            {
                //System.out.println ("paintContacts: 005");
            	drawTarget (g, conPoint, unit);
            }
        }
        
        //System.out.println ("paintContacts: 006");
        
        // Reset the transformation
        g.setTransform(oldTrans);
    }

    /**
     * Paint terrain for the hexes on the map. 
     * @param g The graphics context into which we are drawing.
     * @param firstTrans The transform before all the transforms for drawing contacts, etc
     */
    public void paintTerrain(Graphics2D g)
    {
        Graphics2D			g2 = savedTerrain.createGraphics();
        AffineTransform		g2oldTrans = g2.getTransform();
        AffineTransform		gOldTrans = g.getTransform();
        AffineTransform		winTrans = new AffineTransform(gOldTrans);
        Point2D				unitPos = realForUnit(data.myUnit);

        myLogger.Log("paintTerrain: 001");
        
        // This code is all to detect if our display section is going to go outside our cached image
        // If it is, we have to redraw the map
        // Generally we have to draw more often the closer we are zoomed in... however, since zoomed in means less
        // hexes to draw, the speed hit sort of takes care of itself.
        int					halfWidth = (int) (bounds.getWidth() / 2);
        int					halfHeight = (int) (bounds.getHeight() / 2);
        boolean				redraw = false;
        Point2D				testPt = new Point2D.Float();
        testPt.setLocation(savedTerrainBounds.getWidth() / 2 + savedTerrainCenter.getX() - unitPos.getX(),
                           savedTerrainBounds.getHeight() / 2 + savedTerrainCenter.getY() - unitPos.getY());

        
        if (testPt.getX() + halfWidth > savedTerrainBounds.getWidth())
            redraw = true;
        else if (testPt.getX() - halfWidth < savedTerrainBounds.getX())
            redraw = true;
        else if (testPt.getY() + halfHeight > savedTerrainBounds.getHeight())
            redraw = true;
        else if (testPt.getY() - halfHeight < savedTerrainBounds.getY())
            redraw = true;

        // Do we need to redraw our image?
        if (data.terrainChanged(iMapNum) || redraw)
        {
            myLogger.Log("paintTerrain: 002 redraw");
            
            // Clear out the picture
            g2.setColor(Color.black);
            g2.fill(savedTerrainBounds);
            
            // Set a transform
            if (prefs.xOffset != 0 || prefs.yOffset != 0)
            {
                AffineTransform			xform = new AffineTransform(g2oldTrans);
                xform.translate(-prefs.xOffset * (w + l), -prefs.yOffset * h);
                g2.setTransform(setupStandardTransform(xform, savedTerrainBounds));
            }
            else
            {
                g2.setTransform(setupStandardTransform(g2oldTrans, savedTerrainBounds));
            }

            // Paint the terrain
            paintTerrainGraphics(g2);

            // Clear the changed flag
            data.setTerrainChanged(false, iMapNum);
            
            // Note the center of this picture
            savedTerrainCenter = realForUnit(data.myUnit);

            // Reset the transform
            g2.setTransform(g2oldTrans);

        }

        // Translate the corner of our window to the center of our window
        winTrans.translate(bounds.getWidth() / 2, bounds.getHeight() / 2);
        // Translate the corner of our window to the center of the savedTerrain
        winTrans.translate(-savedTerrainBounds.getWidth() / 2, -savedTerrainBounds.getHeight() / 2);
        // Translate the difference between where we are now and where we were when the image was drawn
        winTrans.translate(savedTerrainCenter.getX() - unitPos.getX(), savedTerrainCenter.getY() - unitPos.getY());

        g.setTransform(winTrans);
        g.drawImage(savedTerrain, null, null);
        g.setTransform(gOldTrans);
        
        myLogger.Log("paintTerrain: OUT");
    }

    /**
      * Do the dirty work of drawing terrain into a graphics object.
      * @param g The graphics context into which we are drawing.
      */
    public void paintTerrainGraphics(Graphics2D g)
    {
        /* Before we do anything, check the list of contacts for dropships. 
         * If there are dropships, change the hexes around them.
         */
    	
        myLogger.Log("paintTerrainGraphics: 001");
        
    	data.validate();
    	
    	/*
        We should go hex by hex from the top left, check what terrain it is, and draw
        the correct color or pattern or whatever for that hex.

        The style of the map may change based on preferences or something, but for now
        we should draw it in a fashion similar to what we get with the text-based map.
        This means terrain type on top l/r and bottom left and elevation on bottom right.

        We could just figure out the coords for the entire hex.
        Then we can create a Polygon object with base coords, translate, and fill for each hex...

        So, the first hex (top leftmost) is translated 0 in x and 0 in y, of course.
        Other hexes are translated:
        
        xtrans = x * (w + ((h/2) * tan30))

        if y even or 0:
          ytrans = y * h
        if y odd:
          ytrans = h/2 + (y * h)
           
        */

        
        AffineTransform			oldTrans = g.getTransform();

        int						hexX = data.myUnit.getX() - (numAcross / 2);
        int						hexY = data.myUnit.getY() - (numDown / 2);

        Point2D					realHex = new Point2D.Float();

        // Account for offset views by moving the view over
        hexX += prefs.xOffset;
        hexY += prefs.yOffset;

        // --------------------
        
        AffineTransform			trans = new AffineTransform();
        
        // Now we go into a loop to draw the proper colors or textures for each terrain
        for (int j = 0; j < numAcross; j++)
        {
            if (hexX + j >= 0)		// Make sure we're drawing within the boundaries of the map
            {
                for (int i = 0; i < numDown; i++)
                {
                    // Make sure we're drawing within the boundaries of the map
                    if (hexY + i >= 0 && data.getHexTerrain(hexX + j, hexY + i) != MUHex.UNKNOWN)
                    {
                        int		hexAbsoluteElevation = data.getHexElevation(hexX + j, hexY + i);
                        // Since we only store images of elevations 0 to 9 (not negative ones)
                        //  we have to use the opposite color for negative elevations like water/water under ice
                        if (hexAbsoluteElevation < 0)
                            hexAbsoluteElevation = -hexAbsoluteElevation;
                        
                        // This gives us the x,y location of this hex
                        getHexToReal(realHex, hexX + j, hexY + i);
                        
                        // Set the transform to our previously setup one
                        trans.setTransform(oldTrans);
                        // Translate to where the hex should be located
                        // We have to compensate by -l for x because the picture in the array is shifted over by +l.
                        trans.translate(realHex.getX() - l, realHex.getY());
                        g.setTransform(trans);
                        // And draw it
                        int hideQuestionMark = 1;
                        if(prefs.tacShowLOSInfo == true) {
                            String hashkey = String.valueOf(hexX + j) + " " + String.valueOf(hexY + i);
	                        if(data.LOSinfo.get(hashkey) != null && (Boolean) data.LOSinfo.get(hashkey) == false) 
	                        	hideQuestionMark = 0;
                        }
                        g.drawImage(imageForTerrain(data.getHexTerrain(hexX + j, hexY + i),
                                                    hexAbsoluteElevation,
                                                    hideQuestionMark),
                                    null,
                                    null);
                        
                        // Set our transform for the rest of the info

                        // Optional stuff -----------------------
                        if (prefs.tacShowHexNumbers)
                        {
                            AffineTransform			beforeNumberRot = g.getTransform();
                            AffineTransform			baseTrans2 = g.getTransform();
                            String					hexString = (hexX + j) + "," + (hexY + i);
                            baseTrans2.rotate(-PI / 2, hexPoly.getX(2), hexPoly.getY(2));
                            g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
                            g.setFont(hexNumberFont);
                            g.setTransform(baseTrans2);
                            g.drawString(hexString,
                                         (float) (hexPoly.getX(2) + 2),
                                         (float) (hexPoly.getY(2) + (hexNumberFont.getLineMetrics(hexString, frc)).getAscent()));
                            g.setTransform(beforeNumberRot);
                        }
                        
                        if (prefs.tacShowCliffs)
                        {
                            Stroke		saveStroke = g.getStroke();
                            int			thisElevation = data.getHexCliffElevation(hexX + j, hexY + i);
                            g.setColor(Color.red);
                            g.setStroke(new BasicStroke(2.0f));		// Make the red line wider
                            
                            int myElevation;
                            int cliffDiff = data.myUnit.cliffDiff();
                                                  	
                           	myElevation = thisElevation;
                            // We are at: hexX + j, hexY + i
                            if ((hexX + j) % 2 == 0)
                            {
                                // Even X
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 0, hexY + i - 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(0), (int) hexPoly.getY(0), (int) hexPoly.getX(5), (int) hexPoly.getY(5));
                                if (Math.abs(data.getHexCliffElevation(hexX + j - 1, hexY + i + 0) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(0), (int) hexPoly.getY(0), (int) hexPoly.getX(1), (int) hexPoly.getY(1));
                                if (Math.abs(data.getHexCliffElevation(hexX + j - 1, hexY + i + 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(1), (int) hexPoly.getY(1), (int) hexPoly.getX(2), (int) hexPoly.getY(2));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 0, hexY + i + 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(2), (int) hexPoly.getY(2), (int) hexPoly.getX(3), (int) hexPoly.getY(3));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 1, hexY + i + 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(3), (int) hexPoly.getY(3), (int) hexPoly.getX(4), (int) hexPoly.getY(4));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 1, hexY + i + 0) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(4), (int) hexPoly.getY(4), (int) hexPoly.getX(5), (int) hexPoly.getY(5));
                            }
                            else
                            {
                                // Odd X
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 0, hexY + i - 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(0), (int) hexPoly.getY(0), (int) hexPoly.getX(5), (int) hexPoly.getY(5));
                                if (Math.abs(data.getHexCliffElevation(hexX + j - 1, hexY + i - 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(0), (int) hexPoly.getY(0), (int) hexPoly.getX(1), (int) hexPoly.getY(1));
                                if (Math.abs(data.getHexCliffElevation(hexX + j - 1, hexY + i + 0) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(1), (int) hexPoly.getY(1), (int) hexPoly.getX(2), (int) hexPoly.getY(2));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 0, hexY + i + 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(2), (int) hexPoly.getY(2), (int) hexPoly.getX(3), (int) hexPoly.getY(3));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 1, hexY + i + 0) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(3), (int) hexPoly.getY(3), (int) hexPoly.getX(4), (int) hexPoly.getY(4));
                                if (Math.abs(data.getHexCliffElevation(hexX + j + 1, hexY + i - 1) - myElevation) > cliffDiff)
                                    g.drawLine((int) hexPoly.getX(4), (int) hexPoly.getY(4), (int) hexPoly.getX(5), (int) hexPoly.getY(5));
                            }

                            g.setStroke(saveStroke);				// Restore the old stroke
                        }
                    }
                }                
            }
        }

        // Reset the transformation
        g.setTransform(oldTrans);
        
        myLogger.Log("paintTerrainGraphics: OUT");
    }

    /**
      * Setup the transformation that recenters tactical, contacts, and our own unit in the main window.
      * @param base The current transform that we want to modify
      */
    protected AffineTransform setupStandardTransform(AffineTransform base, Rectangle whichBounds)
    {
        AffineTransform			newTrans = new AffineTransform(base);
        Point2D					unitDraw = realForUnit(data.myUnit);
	
        // Do some translation magic so that our unit is always in the exact center of the screen
        // Translate our own unit to (0,0), then translate out to the middle of the window
        newTrans.translate(-unitDraw.getX() + whichBounds.width/2,
                           -unitDraw.getY() + whichBounds.height/2);

        return newTrans;
    }

    /**
      * Return a Point2D representing the real location of any unit. Used for drawing and translating.
      * @param unit The unit we are getting a real point for
      */
    protected Point2D realForUnit(MUUnitInfo unit)
    {
        return getMapToReal(unit.position.getFX(), unit.position.getFY());
    }

    /**
     * Returns a Point2D representing the (upper left corner) screen
     * coordinates for a given hex.
     */
    protected Point2D getHexToReal (final MUPoint pt) {
        return getMapToReal(pt.getCenterFX() - MUConstants.ALPHA,
                            pt.getCenterFY() - 0.5f);
    }

    /**
     * Sets a Point2D representing the (upper left corner) screen coordinates
     * for a given hex.
     */
    public void getHexToReal (final Point2D rv, final int hx, final int hy) {
        pvtMapPt.setHexLocation(hx, hy);

        // XXX: MUPoint map coordinates go as low (up) as -0.5, but the old
        // hexToReal() started from 0, so we need to shift fy by +0.5.  Then we
        // shift back by -0.5 to get the upper left corner, resulting in no net
        // shift.
        rv.setLocation(h * (pvtMapPt.getCenterFX() - MUConstants.ALPHA),
                       h * (pvtMapPt.getCenterFY()));
    }

    /**
     * Transform component coordinates to center-relative coordinates.
     */
    public void getScreenToOffset (final Point rv, final int x, final int y) {
        rv.setLocation(x - bounds.width / 2, y - bounds.height / 2);
    }

    /**
     * Transform center-relative coordinates to map coordinates.
     * TODO: We can cache 1/h, but the optimizer can do it for us, can't it?
     */
    public void getOffsetToMap (final Point2D rv, final Point offsetPt) {
        final float myFX = data.myUnit.position.getFX();
        final float myFY = data.myUnit.position.getFY();

        rv.setLocation(myFX + (1f / h) * (float)offsetPt.x,
                       myFY + (1f / h) * (float)offsetPt.y);
    }

    /**
     * Convenience method to transform map coordinates to hex coordinates.
     * TODO: Might add this as a static method in MUPoint or something.
     */
    public void getMapToHex (final Point rv, final Point2D mapPt) {
        pvtMapPt.setLocation(mapPt);

        rv.setLocation(pvtMapPt.getHexX(), pvtMapPt.getHexY());
    }

    // TODO: Temporary stuff to get rid of after coordinate cleanup.
    private final MUPoint pvtMapPt = new MUPoint ();

    private Point2D getMapToReal (final float fx, final float fy) {
        // XXX: MUPoint map coordinates go as low (up) as -0.5, but the old
        // hexToReal() started from 0, so we need to shift fy by +0.5.
        return new Point2D.Float (h * fx, h * (fy + 0.5f));
    }
    
    /**
      * Get the proper color to describe a terrain character.
      */
    public Color colorForTerrain(int terrain)
    {
        return prefs.terrainColors[terrain];
    }

    /**
      * Get the Image that we want to copy for a given terrain character.
      */    
    protected BufferedImage imageForTerrain(int terrain, int elevation, int los)
    {
        return hexImages[terrain][elevation][los];
    }

    /**
     * Paint the numbers which tell us which hexes are on the map.
     * @param g The graphics context into which we are drawing.
     */
    public void paintNumbers(Graphics2D g)
    {
        AffineTransform			oldTrans = g.getTransform();
        AffineTransform			winTrans;
        AffineTransform			trans = new AffineTransform();
        
        Rectangle				barRect;
        Point2D					realHex = new Point2D.Float();
        Point2D					unitPos = realForUnit(data.myUnit);
        
        int						startX = (int) (data.myUnit.getX() - (numAcross / 2) + prefs.xOffset);
        int						startY = (int) (data.myUnit.getY() - (numDown / 2) + prefs.yOffset);
        int						endX = startX + numAcross;
        int						endY = startY + numDown;
        int						skip = 1;

        Rectangle2D				stringRectX = hexNumberFont.getStringBounds(Integer.toString(endX), frc);
        Rectangle2D				stringRectY = hexNumberFont.getStringBounds(Integer.toString(endY), frc);

        if (endX > endY)
            barHeight = (int) stringRectX.getHeight() + 2;
        else
            barHeight = (int) stringRectY.getHeight() + 2;
        
        if (startX < 0)
            startX = 0;
        if (startY < 0)
            startY = 0;

        if (h <= 5)
            skip = 5;
        else if (h <= 10)
            skip = 3;
        else if (h <= 20)
            skip = 2;
        else
            skip = 1;

        // If we're skipping by 5, we do it on multiples of 5... etc
        if (skip != 1)
        {
            startX -= startX % skip;
            startY -= startY % skip;            
        }
        
        // Set the proper font
        g.setFont(hexNumberFont);
        
        // Bar along side
        barRect = new Rectangle(0, 0, barHeight, bounds.height - barHeight);
        g.setColor(Color.black);
        g.fill(barRect);
        g.setColor(Color.lightGray);
        g.drawLine(barRect.width, 0, barRect.width, barRect.height);

        // Set the proper window transform
        winTrans = new AffineTransform(oldTrans);
        winTrans.translate(0, -unitPos.getY() + bounds.getHeight() / 2);
        g.setTransform(winTrans);
        
        // Numbers along the side
        for (int i = startY; i <= endY; i += skip)
        {
            if (i % 2 == 0)
                g.setColor(Color.white);
            else
                g.setColor(Color.lightGray);

            trans.setTransform(winTrans);
            getHexToReal(realHex, 0, i - prefs.yOffset);
            trans.translate(4, realHex.getY());
            trans.rotate(PI / 2);
            g.setTransform(trans);
            g.drawString(Integer.toString(i), 0, 0);
        }
        
        // Reset transform
        g.setTransform(oldTrans);
        
        // Bar along top
        barRect = new Rectangle(0, 0, bounds.width, barHeight);
        g.setColor(Color.black);
        g.fill(barRect);
        g.setColor(Color.lightGray);
        g.drawLine(barRect.x, barRect.height, barRect.width, barRect.height);

        // Set the proper window transform
        winTrans = new AffineTransform(oldTrans);
        winTrans.translate(-unitPos.getX() + bounds.getWidth() / 2, 0);
        g.setTransform(winTrans);

        // Numbers along the top
        for (int i = startX; i <= endX; i += skip)
        {
            trans.setTransform(winTrans);
            
            if (i % 2 == 0)
                g.setColor(Color.white);
            else
                g.setColor(Color.lightGray);

            // Need to adjust for funky hex arrangement (ie, even hexes higher than odds)
            if ((i - prefs.xOffset) % 2 == 0)
                trans.translate(0, h/2);
            
            getHexToReal(realHex, i - prefs.xOffset, 0);
            trans.translate(realHex.getX(), -realHex.getY() + stringRectY.getHeight() - 2);
            g.setTransform(trans);
            g.drawString(Integer.toString(i), 0, 0);
        }

        g.setTransform(oldTrans);
    }

    // ----------------------
    // Helper functions
    // ----------------------

    /**
     * Draw a white box for blanking out terrain or whatever. Then draw ID into it.
     * @param g The graphics context into which we are drawing.
     * @param unit The unit we're drawing
     * @param self If true, we're drawing our own unit
     */
    public void drawIDBox(Graphics2D g, MUUnitInfo unit, Point2D pt, boolean self)
    {
        try
        {   
            AffineTransform		oldTrans = g.getTransform();
            Stroke				oldStroke = g.getStroke();
            float				borderWidth = 4f;
            float				borderHeight = 4f;
            float				spacing = 5f;		// Distance between icon and name

            // Setup this transform

            AffineTransform		contactMove = new AffineTransform(oldTrans);
            
            double x1;
            double y1;
            
            x1 = pt.getX () - h/4f;
            y1 = pt.getY () - h/4f;

            myLogger.Log("drawIDBox: 001 (" + unit.id +
            	") (" + pt.getX () + "," + x1 + "," + (h/4f) +
            	") (" + pt.getY () + "," + y1 + "," + (h/4f));
            
            myLogger.Log("unitLoc (" +
            	unit.getX () + "," + unit.getY () + ")");
            
            contactMove.translate(pt.getX() - h/4f, pt.getY() - h/4f);
            g.setTransform(contactMove);
            
            // Draw icon
            if (unit.isDestroyed())
                g.setColor(Color.lightGray);		// destroyed
            else if (unit.isFriend())
                g.setColor(Color.black);			// or green
            else
                g.setColor(Color.red);			// or yellow

            Rectangle2D			iconBounds = new Rectangle2D.Float(0, 0, h/2, h/2);

            if (self)
                g.setStroke(new BasicStroke(2));

            g.draw(unit.icon(h / 2, false));

            if (self)
                g.setStroke(oldStroke);
            
            if (!self)
            {
                // Draw text box
                Rectangle2D			backingBox;
                StringBuffer		nameString = new StringBuffer();

                if (unit.id.length() <= 2)
                {
                    nameString.append('[');
                    nameString.append(unit.id);
                    nameString.append(']');
                }

                if (prefs.tacShowUnitNames)
                {
                    nameString.append(' ');
                    nameString.append(unit.name);
                }

                Rectangle2D 		stringRect = infoFont.getStringBounds(nameString.toString(), frc);
    
                backingBox = new Rectangle2D.Float((float) (iconBounds.getWidth() / 2 + spacing + borderWidth / 2),
                                                   (float) (-stringRect.getHeight() / 2 - borderHeight / 2),
                                                   (float) (stringRect.getWidth() + borderWidth),
                                                   (float) (stringRect.getHeight() + borderHeight));                
                
                if (!unit.isDestroyed())
                {
                    if (unit.isFriend())
                        g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
                    else
                        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));

                    g.fill(backingBox);
                    g.setColor(Color.black);
                    g.draw(backingBox);
                }
                else
                {
                    g.setColor(Color.lightGray);
                    g.draw(backingBox);

                    Line2D		line1 = new Line2D.Float((float) backingBox.getX(),
                                                     (float) backingBox.getY(),
                                                     (float) (backingBox.getX() + backingBox.getWidth()),
                                                     (float) (backingBox.getY() + backingBox.getHeight()));
                    Line2D		line2 = new Line2D.Float((float) backingBox.getX(),
                                                     (float) (backingBox.getY() + backingBox.getHeight()),
                                                     (float) (backingBox.getX() + backingBox.getWidth()),
                                                     (float) backingBox.getY());
                    
                    g.setColor(new Color(255, 0, 0, 80));
                    g.draw(line1);
                    g.draw(line2);
                }
                        
                // Draw contact's ID string
                if (unit.isOld() || unit.isDestroyed())
                    g.setColor(Color.darkGray);
                else if (unit.isFriend())
                    g.setColor(Color.black);
                else if (unit.isTarget())
                    g.setColor(Color.red);
                else
                    g.setColor(Color.yellow);
                
                g.setFont(infoFont);
                g.drawString(nameString.toString(),
                            (float) (iconBounds.getWidth() / 2 + spacing + borderWidth),
                            (float) (stringRect.getHeight() / 2 + (infoFont.getLineMetrics(nameString.toString(), frc)).getDescent()) - borderHeight);
            }
            else
            {
                // Floating Indicators
                if (prefs.tacShowIndicators)
                {
                    int indOffset = h;
                    // Heat indicator
                    if (unit.hasHeat())
                    {
                        int heatLength = data.myUnit.heat / 10;
                        if (heatLength > 50)
                            heatLength = 50;

                        Rectangle heatRect = new Rectangle(-(heatBarMaxLength - h/2)/2, indOffset, heatLength, 8);

                        if (data.myUnit.heat - data.myUnit.heatDissipation >= 200)
                            g.setColor(Color.red);
                        //g.setColor(new Color(255, 0, 0, 30));
                        else if (data.myUnit.heat - data.myUnit.heatDissipation >= 140)
                            g.setColor(Color.yellow);
                        else
                            g.setColor(Color.green);

                        g.fill(heatRect);
                        g.setColor(Color.black);
                        g.drawRect(-(heatBarMaxLength - h/2)/2, indOffset, heatBarMaxLength, 8);
                        indOffset += 10;

                    }

                    // Armor status
                    float armorLeft = data.myUnit.percentArmorLeft();
                    Rectangle armorRect = new Rectangle(-(heatBarMaxLength - h/2)/2,
                                                        indOffset, (int) (armorLeft * heatBarMaxLength /100), 8);
                    g.setColor(MUUnitInfo.colorForPercent(armorLeft));
                    g.fill(armorRect);
                    g.setColor(Color.black);
                    g.drawRect(-(heatBarMaxLength - h/2)/2, indOffset, heatBarMaxLength, 8);
                    indOffset += 10;

                    // Internal status
                    float               internalLeft = data.myUnit.percentInternalLeft();
                    Rectangle       internalRect = new Rectangle(-(heatBarMaxLength - h/2)/2,
                                                                 indOffset, (int) (internalLeft * heatBarMaxLength /100), 8);
                    g.setColor(MUUnitInfo.colorForPercent(internalLeft));
                    g.fill(internalRect);
                    g.setColor(Color.black);
                    g.drawRect(-(heatBarMaxLength - h/2)/2, indOffset, heatBarMaxLength, 8);
                    indOffset += 10;
                }
                
                if (prefs.tacShowArcs)
                {
                    AffineTransform			arcXform = new AffineTransform(g.getTransform());
                    float					headingDeg = -data.myUnit.heading - 180.0f;

                    // Accomodate for rotate torso
                    if (data.myUnit.type.equals("B"))
                        headingDeg -= data.myUnit.turretHeading;
                    
                    arcXform.translate(h / 4, h / 4);
                    g.setTransform(arcXform);

                    // This is our own unit. Let's draw the helper 'arcs'
                    float	r = prefs.arcIndicatorRange * h;			// r = radius

                    Arc2D	frontArc = new Arc2D.Float();
                    Arc2D	leftArc = new Arc2D.Float();
                    Arc2D	rightArc = new Arc2D.Float();
                    Arc2D	rearArc = new Arc2D.Float();

                    frontArc.setArcByCenter(0,
                                            0,
                                            prefs.makeArcsWeaponRange ? data.myUnit.maxFrontRange() * h : r,
                                            -30.0 + headingDeg,
                                            -120.0,
                                            Arc2D.PIE);
                    g.setColor(new Color(0, 0, 255, 30));			// faded blue
                    //g.draw(frontArc);
                    g.fill(frontArc);

                    leftArc.setArcByCenter(0,
                                           0,
                                           prefs.makeArcsWeaponRange ? data.myUnit.maxLeftRange() * h : r,
                                           -30.0 + headingDeg,
                                           60.0,
                                           Arc2D.PIE);
                    rightArc.setArcByCenter(0,
                                            0,
                                            prefs.makeArcsWeaponRange ? data.myUnit.maxRightRange() * h : r,
                                            -150 + headingDeg,
                                            -60.0,
                                            Arc2D.PIE);
                    g.setColor(new Color(255, 255, 0, 30));			// faded yellow
                    //g.draw(leftArc);
                    //g.draw(rightArc);
                    g.fill(leftArc);
                    g.fill(rightArc);

                    rearArc.setArcByCenter(0,
                                           0,
                                           prefs.makeArcsWeaponRange ? data.myUnit.maxRearRange() * h : r,
                                           30.0 + headingDeg,
                                           120.0,
                                           Arc2D.PIE);
                    g.setColor(new Color(255, 0, 0, 30));			// faded red
                    //g.draw(rearArc);
                    g.fill(rearArc);
                }
            }
            g.setTransform(oldTrans);

        }
        catch (Exception e)
        {
            System.out.println("Error: drawIDBox: " + e);
        }
    }

    /**
     * Draw a heading indicator line.
     * NOTE: The transformation should be set before entering this function.
     * We will concatenate the transform with our own. It needs to be translated for the numbers
     * along the side and top.
     * @param g The graphics context into which we are drawing
     * @param pt The point at which we are drawing
     * @param u The unit we're dealing with
     * @param type Which type of heading to draw
     */   
    public void drawHeading(Graphics2D g, Point2D pt, MUUnitInfo u, int type)
    {
        AffineTransform		trans = new AffineTransform();
        AffineTransform		oldTrans = g.getTransform();

        float				speedDivisor = 10.75f * prefs.speedIndicatorLength;			// 32.25 = 3 MP, 10.75 = 1 MP
        int					lineLength = h / (int) speedDivisor;

        int					whichHeading;
        float				headingRad;

        // If we're drawing an installation, don't draw a heading
        if (u.type.equals("i"))
            return;
        
        if (type == HEADING_NORMAL)
        {
            whichHeading = u.heading;
            if (u.isDestroyed())
                g.setColor(Color.lightGray);
            else
                g.setColor(Color.black);
        }
        else if (type == HEADING_DESIRED)
        {
            whichHeading = u.desiredHeading;
            g.setColor(Color.blue);
        }
        else if (type == HEADING_JUMP)
        {
            whichHeading = u.jumpHeading;
            g.setColor(Color.green);
        }
        else if (type == HEADING_TURRET)
        {
            whichHeading = (u.heading + u.turretHeading) + 180;
            g.setColor(Color.red);
        }
        else
            return;			// No heading, or done, or something
            
        headingRad = (float) ((whichHeading / 180f) * PI + PI);
        lineLength = (int) (h * ((u.speed == 0 ? speedDivisor : u.speed) / speedDivisor));
        
        // Set up our transformation
        trans.concatenate(g.getTransform());
        trans.rotate(headingRad, pt.getX(), pt.getY());
        trans.translate(pt.getX(), pt.getY());

        // Set the transform        
        g.setTransform(trans);
        
        // And finally, draw it

        g.drawLine(0, 0, 0, lineLength);
        g.setColor(Color.white);
        g.drawLine(1, 0, 1, lineLength);
        g.drawLine(-1, 0, -1, lineLength);

        // Reset the transformation
        g.setTransform(oldTrans);
    }

    /**
     * Draws the status bar at the bottom of the tactical display window.
     * @param g The graphics context.
     */
    public void paintStatusBar(Graphics2D g)
    {
        // Need to figure out a better way to specify what's in the status bar and how to space it without relying on hardcoded magic numbers
        // Perhaps keep a counter of where the next 'item' should be placed, and add to it the length of whatever we just drew plus some spacing
        
        AffineTransform			oldTrans = g.getTransform();
        Rectangle				barRect = new Rectangle(0, bounds.height - barHeight, bounds.width, barHeight);
        String					tempString;
        Rectangle2D				tempRect;
        
        // We assume that barRect.x = 0 for this
        int						nextStartsAt = 10;
        int						spacingDiff = 15;

        g.setFont(tacStatusFont);
        
        g.setColor(Color.black);
        g.fill(barRect);
        g.setColor(Color.lightGray);
        g.drawLine(barRect.x, barRect.y, barRect.width, barRect.y);

        // Zoom buttons
        g.setColor(Color.white);
        g.drawString("-", nextStartsAt, barRect.y + 12);
        nextStartsAt += spacingDiff;
        g.drawString("+", nextStartsAt, barRect.y + 12);
        nextStartsAt += spacingDiff;
        
        // Sep
        g.setColor(Color.darkGray);
        g.drawLine(nextStartsAt, barRect.y + 1, nextStartsAt, barRect.y + barHeight);
        g.setColor(Color.black);
        nextStartsAt += spacingDiff;

        // Heat indicator
        int			heatLength = data.myUnit.heat / 10;
        if (heatLength > 50)
            heatLength = 50;
        Rectangle	heatRect = new Rectangle(nextStartsAt, barRect.y + 4, heatLength, 8);
        Rectangle	totalHeatRect = new Rectangle(nextStartsAt, barRect.y + 4, heatBarMaxLength, 8);

        if (data.myUnit.heat - data.myUnit.heatDissipation >= 200)
            g.setColor(Color.red);
        else if (data.myUnit.heat - data.myUnit.heatDissipation >= 140)
            g.setColor(Color.yellow);
        else
            g.setColor(Color.green);
        g.fill(heatRect);

        g.setColor(Color.white);
        g.draw(totalHeatRect);
        nextStartsAt += heatBarMaxLength + spacingDiff;

        // Sep
        g.setColor(Color.darkGray);
        g.drawLine(nextStartsAt, barRect.y + 1, nextStartsAt, barRect.y + barHeight);
        g.setColor(Color.black);
        nextStartsAt += spacingDiff;

        // Unit information
        tempString = "[" + data.myUnit.id + "] " + data.myUnit.name + " (" + data.myUnit.ref + ")" + " S:" + data.myUnit.status;
        tempRect = tacStatusFont.getStringBounds(tempString, frc);

        g.setColor(Color.white);
        g.drawString(tempString, nextStartsAt, barRect.y + 11);
        nextStartsAt += tempRect.getWidth() + spacingDiff;

        // Sep
        g.setColor(Color.darkGray);
        g.drawLine(nextStartsAt, barRect.y + 1, nextStartsAt, barRect.y + barHeight);
        g.setColor(Color.black);
        nextStartsAt += spacingDiff;

        // Armor status
        float		armorLeft = data.myUnit.percentArmorLeft();
        g.setColor(MUUnitInfo.colorForPercent(armorLeft));
        tempString = armorLeft + "% / ";
        tempRect = tacStatusFont.getStringBounds(tempString, frc);
        g.drawString(tempString, nextStartsAt, barRect.y + 11);
        nextStartsAt += tempRect.getWidth();	// Don't put in spacing here, the space is there in the string

        // Internal status
        float		internalLeft = data.myUnit.percentInternalLeft();
        g.setColor(MUUnitInfo.colorForPercent(internalLeft));
        tempString = internalLeft + "%";
        tempRect = tacStatusFont.getStringBounds(tempString, frc);
        g.drawString(tempString, nextStartsAt, barRect.y + 11);
        nextStartsAt += tempRect.getWidth() + spacingDiff;

        // Sep
        g.setColor(Color.darkGray);
        g.drawLine(nextStartsAt, barRect.y + 1, nextStartsAt, barRect.y + barHeight);
        g.setColor(Color.black);
        nextStartsAt += spacingDiff;

        // Location information
        tempString = "Loc: " + data.myUnit.getX() + ", " + data.myUnit.getY() + ", " + data.myUnit.getZ();
        tempRect = tacStatusFont.getStringBounds(tempString, frc);
        g.setColor(Color.white);
        g.drawString(tempString, nextStartsAt, barRect.y + 11);
        nextStartsAt += tempRect.getWidth() + spacingDiff;

        // Sep
        g.setColor(Color.darkGray);
        g.drawLine(nextStartsAt, barRect.y + 1, nextStartsAt, barRect.y + barHeight);
        g.setColor(Color.black);
        nextStartsAt += spacingDiff;
                
        tempString = "Weather: " + MUWeather.lightString(data.weather.light) + 
        			 " : Vis " + data.weather.visibility + 
        			 " : Temp " + data.weather.ambientTemperature +
        			 "\u00B0C : Gravity " + data.weather.gravity + "%";
        tempRect = tacStatusFont.getStringBounds(tempString, frc);
        g.setColor(Color.white);
        g.drawString(tempString, nextStartsAt, barRect.y + 11);
        nextStartsAt += tempRect.getWidth() + spacingDiff;
        
        if(data.weather.isVacuum) {
            tempString = "VACUUM";
			tempRect = tacStatusFont.getStringBounds(tempString, frc);
			g.setColor(Color.red);
			g.drawString(tempString, nextStartsAt, barRect.y + 11);
			nextStartsAt += tempRect.getWidth() + spacingDiff;        	
        }
        
        if(data.weather.isUnderground) {
            tempString = "UNDERGROUND";
			tempRect = tacStatusFont.getStringBounds(tempString, frc);
			g.setColor(Color.red);
			g.drawString(tempString, nextStartsAt, barRect.y + 11);
			nextStartsAt += tempRect.getWidth() + spacingDiff;        	
        }
        
        if(data.weather.isDark) {
            tempString = "DARK";
			tempRect = tacStatusFont.getStringBounds(tempString, frc);
			g.setColor(Color.red);
			g.drawString(tempString, nextStartsAt, barRect.y + 11);
			nextStartsAt += tempRect.getWidth() + spacingDiff;        	
        }
        	        
        // Reset transform
        g.setTransform(oldTrans);
    }
    
    /**
     * Draws the armor diagram.
     * @param g The graphics context.
     */
    public void paintArmorDiagram(Graphics2D g)
    {
        AffineTransform xform = g.getTransform();
        int armorScale = 2;	// Size of the armor/internal indicators
        xform.translate(barHeight, bounds.height - 2*barHeight - armorScale * 20);	// Scale of mech drawing is 0-20 pixels
        xform.scale(armorScale,armorScale);	// resize the picture
        g.setTransform(xform);


        int armorTransparency = 185;

        // Determine what type of diagram to show.
        // Presently unsupported: F=AeroFighter, A=AeroDyne Dropship, D=Spheroid Dropship, S=BattleSuit, I=Infantry
        
    	if(data.myUnit.type.equals("B")) { // biped
    		armorBiped(g, armorTransparency);
    	} else if (data.myUnit.type.equals("Q")) { // quad
    		armorQuad(g, armorTransparency);
    	} else if (data.myUnit.type.equals("T") || // tracked
    				data.myUnit.type.equals("H") || // hover
    				data.myUnit.type.equals("W") || // wheeled
    				data.myUnit.type.equals("N") || // Naval surface displacement
    				data.myUnit.type.equals("Y") || // Naval hydrofoil
    				data.myUnit.type.equals("U") || // Naval submarine
    				data.myUnit.type.equals("i")) { // Installation 
    		armorGround(g, armorTransparency);
    	} else if (data.myUnit.type.equals("V")) { // VTOL
    		armorVTOL(g, armorTransparency);
    	}

    }
    
    /** Paint a biped armor diagram.
     * 
     * @param g Graphics context.
     * @param armorTransparency Alpha of diagram.
     */
    private void armorBiped(Graphics2D g, int armorTransparency) {
    	AffineTransform xform = g.getTransform();
        float armorLeft;
        // Draw Head Armor
        GeneralPath hOutline = new GeneralPath();
        hOutline.moveTo( 9, 0);
        hOutline.lineTo( 8, 3);
        hOutline.lineTo(12, 3);
        hOutline.lineTo(11, 0);
        hOutline.lineTo( 9, 0);

        g.setColor(Color.darkGray);
        g.draw(hOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("H"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(hOutline);

        // Draw Center Torso Armor
        GeneralPath ctOutline = new GeneralPath();
        ctOutline.moveTo( 8, 3);
        ctOutline.lineTo(10,12);
        ctOutline.lineTo(12, 3);
        ctOutline.lineTo( 8, 3);

        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);

        // Draw Right Torso Armor
        GeneralPath rtOutline = new GeneralPath();
        rtOutline.moveTo(12, 3);
        rtOutline.lineTo(15, 3);
        rtOutline.lineTo(13, 11);
        rtOutline.lineTo(10, 12);
        rtOutline.lineTo(12,  3);

        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor
        GeneralPath ltOutline = new GeneralPath();
        ltOutline.moveTo( 5, 3);
        ltOutline.lineTo( 7,11);
        ltOutline.lineTo(10,12);
        ltOutline.lineTo( 8, 3);
        ltOutline.lineTo( 5, 3);

        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Left Arm Armor
        GeneralPath laOutline = new GeneralPath();
        laOutline.moveTo( 5, 3);
        laOutline.lineTo( 1, 8);
        laOutline.lineTo( 4,10);
        laOutline.lineTo( 6, 7);
        laOutline.lineTo( 5, 3);

        g.setColor(Color.darkGray);
        g.draw(laOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LA"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(laOutline);

        // Draw Right Arm Armor
        GeneralPath raOutline = new GeneralPath();
        raOutline.moveTo(15, 3);
        raOutline.lineTo(14, 7);
        raOutline.lineTo(16,10);
        raOutline.lineTo(19, 8);
        raOutline.lineTo(15, 3);

        g.setColor(Color.darkGray);
        g.draw(raOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RA"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(raOutline);

        // Draw Left Leg Armor
        GeneralPath llOutline = new GeneralPath();
        llOutline.moveTo( 7,11);
        llOutline.lineTo( 4,20);
        llOutline.lineTo( 8,20);
        llOutline.lineTo(10,12);
        llOutline.lineTo( 7,11);

        g.setColor(Color.darkGray);
        g.draw(llOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(llOutline);

        // Draw Right Leg Armor
        GeneralPath rlOutline = new GeneralPath();
        rlOutline.moveTo(10,12);
        rlOutline.lineTo(12,20);
        rlOutline.lineTo(16,20);
        rlOutline.lineTo(13,11);
        rlOutline.lineTo(10,12);

        g.setColor(Color.darkGray);
        g.draw(rlOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rlOutline);

        // Draw Rear armor
        // ***************
        xform.translate(15, 0);
        g.setTransform(xform);

        // Draw Center Torso Armor
        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);	// ctOutline points already defined above

        // Draw Right Torso Armor Rear
        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor Rear
        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Internals
        // **************
        xform.translate(15, 0);
        g.setTransform(xform);

        // Draw Head Internals
        g.setColor(Color.darkGray);
        g.draw(hOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("H"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(hOutline);

        // Draw Center Torso Internals
        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);	// ctOutline points already defined above

        // Draw Right Torso Armor Internals
        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor Internals
        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Left Arm Internals
        g.setColor(Color.darkGray);
        g.draw(laOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LA"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(laOutline);

        // Draw Right Arm Internals
        g.setColor(Color.darkGray);
        g.draw(raOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RA"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(raOutline);

        // Draw Left Leg Internals
        g.setColor(Color.darkGray);
        g.draw(llOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(llOutline);

        // Draw Right Leg Internals
        g.setColor(Color.darkGray);
        g.draw(rlOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rlOutline);	    
    }
    
    /**
     * Draw an armor diagram for a quad unit.
     */
    private void armorQuad(Graphics2D g, int armorTransparency) { 
    	AffineTransform xform = g.getTransform();
        float armorLeft;
        // Draw Head Armor
        GeneralPath hOutline = new GeneralPath();
        hOutline.moveTo( 9, 5);
        hOutline.lineTo( 8, 8);
        hOutline.lineTo(12, 8);
        hOutline.lineTo(11, 5);
        hOutline.lineTo( 9, 5);

        g.setColor(Color.darkGray);
        g.draw(hOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("H"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(hOutline);

        // Draw Center Torso Armor
        GeneralPath ctOutline = new GeneralPath();
        ctOutline.moveTo( 8, 8);
        ctOutline.lineTo(10, 12);
        ctOutline.lineTo(12, 8);
        ctOutline.lineTo( 8, 8);

        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);

        // Draw Right Torso Armor
        GeneralPath rtOutline = new GeneralPath();
        rtOutline.moveTo(12, 8);
        rtOutline.lineTo(15, 8);
        rtOutline.lineTo(13, 11);
        rtOutline.lineTo(10, 12);
        rtOutline.lineTo(12,  8);

        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor
        GeneralPath ltOutline = new GeneralPath();
        ltOutline.moveTo( 5, 8);
        ltOutline.lineTo( 7, 11);
        ltOutline.lineTo(10, 12);
        ltOutline.lineTo( 8, 8);
        ltOutline.lineTo( 5, 8);

        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Rear Left Leg Armor
        GeneralPath rllOutline = new GeneralPath();
        rllOutline.moveTo( 5, 8);
        rllOutline.lineTo( 1, 20);
        rllOutline.lineTo( 5, 20);
        rllOutline.lineTo( 7, 11);
        rllOutline.lineTo( 5, 8);

        g.setColor(Color.darkGray);
        g.draw(rllOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RLL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rllOutline);

        // Draw Rear Right Leg Armor
        GeneralPath rrlOutline = new GeneralPath();
        rrlOutline.moveTo(15, 8);
        rrlOutline.lineTo(13, 11);
        rrlOutline.lineTo(15, 20);
        rrlOutline.lineTo(19, 20);
        rrlOutline.lineTo(15, 8);

        g.setColor(Color.darkGray);
        g.draw(rrlOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RRL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rrlOutline);

        // Draw Front Left Leg Armor
        GeneralPath fllOutline = new GeneralPath();
        fllOutline.moveTo( 7,11);
        fllOutline.lineTo( 5,20);
        fllOutline.lineTo( 9,20);
        fllOutline.lineTo(10,12);
        fllOutline.lineTo( 7,11);

        g.setColor(Color.darkGray);
        g.draw(fllOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("FLL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fllOutline);

        // Draw Front Right Leg Armor
        GeneralPath frlOutline = new GeneralPath();
        frlOutline.moveTo(10,12);
        frlOutline.lineTo(11,20);
        frlOutline.lineTo(15,20);
        frlOutline.lineTo(13,11);
        frlOutline.lineTo(10,12);

        g.setColor(Color.darkGray);
        g.draw(frlOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("FRL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(frlOutline);

        // Draw Rear armor
        // ***************
        xform.translate(15, 0);
        g.setTransform(xform);

        // Draw Center Torso Armor
        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);	// ctOutline points already defined above

        // Draw Right Torso Armor Rear
        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor Rear
        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentRearArmorLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Internals
        // **************
        xform.translate(15, 0);
        g.setTransform(xform);

        // Draw Head Internals
        g.setColor(Color.darkGray);
        g.draw(hOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("H"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(hOutline);

        // Draw Center Torso Internals
        g.setColor(Color.darkGray);
        g.draw(ctOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("CT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ctOutline);	// ctOutline points already defined above

        // Draw Right Torso Armor Internals
        g.setColor(Color.darkGray);
        g.draw(rtOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rtOutline);

        // Draw Left Torso Armor Internals
        g.setColor(Color.darkGray);
        g.draw(ltOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LT"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(ltOutline);

        // Draw Rear Left Leg Internals
        g.setColor(Color.darkGray);
        g.draw(rllOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RLL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rllOutline);

        // Draw Rear Right Leg Internals
        g.setColor(Color.darkGray);
        g.draw(rrlOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RRL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rrlOutline);

        // Draw Front Left Leg Internals
        g.setColor(Color.darkGray);
        g.draw(fllOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("FLL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fllOutline);

        // Draw Front Right Leg Internals
        g.setColor(Color.darkGray);
        g.draw(frlOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("FRL"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(frlOutline);	    
    }

    /**
     * Draw an armor diagram for a ground unit.
     */
    private void armorGround(Graphics2D g, int armorTransparency) { 
    	AffineTransform xform = g.getTransform();
        float armorLeft;
        // Draw Front Side Armor
        GeneralPath fsOutline = new GeneralPath();
        fsOutline.moveTo( 1, 0);
        fsOutline.lineTo(14, 0);
        fsOutline.lineTo(11, 5);
        fsOutline.lineTo( 4, 5);
        fsOutline.lineTo( 1, 0);

        g.setColor(Color.darkGray);
        g.draw(fsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("FS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fsOutline);

        // Draw Turret Armor
        GeneralPath tOutline = new GeneralPath();
        tOutline.moveTo( 4,  5);
        tOutline.lineTo(11,  5);
        tOutline.lineTo(11, 16);
        tOutline.lineTo( 4, 16);
        tOutline.lineTo( 4,  5);

        g.setColor(Color.darkGray);
        g.draw(tOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("T"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(tOutline);
        
        // Draw Left Side Armor
        GeneralPath lsOutline = new GeneralPath();
        lsOutline.moveTo( 1,  0);
        lsOutline.lineTo( 4,  5);
        lsOutline.lineTo( 4, 16);
        lsOutline.lineTo( 1, 20);
        lsOutline.lineTo( 1,  0);

        g.setColor(Color.darkGray);
        g.draw(lsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(lsOutline);
        
        // Draw Right Side Armor
        GeneralPath rsOutline = new GeneralPath();
        rsOutline.moveTo( 14,  0);
        rsOutline.lineTo( 11,  5);
        rsOutline.lineTo( 11, 16);
        rsOutline.lineTo( 14, 20);
        rsOutline.lineTo( 14,  0);
        
        g.setColor(Color.darkGray);
        g.draw(rsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rsOutline); 
        
        // Draw Aft Side Armor
        GeneralPath asOutline = new GeneralPath();
        asOutline.moveTo( 4, 16);
        asOutline.lineTo(11, 16);
        asOutline.lineTo(14, 20);
        asOutline.lineTo( 1, 20);
        asOutline.lineTo( 4, 16);
        
        g.setColor(Color.darkGray);
        g.draw(asOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("AS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(asOutline);
        

        // Draw Internals
        // **************
        xform.translate(20, 0);
        g.setTransform(xform);

        // Draw Front Side Internals
        g.setColor(Color.darkGray);
        g.draw(fsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("FS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fsOutline);

        // Draw Turret Internals
        g.setColor(Color.darkGray);
        g.draw(tOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("T"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(tOutline);
        
        // Draw Left Side Internals
        g.setColor(Color.darkGray);
        g.draw(lsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(lsOutline);	
        
        // Draw Right Side Internals
        g.setColor(Color.darkGray);
        g.draw(rsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rsOutline);
        
        // Draw Aft Side Internals
        g.setColor(Color.darkGray);
        g.draw(asOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("AS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(asOutline);	
    }
    
    private void armorVTOL(Graphics2D g, int armorTransparency) { 
    	AffineTransform xform = g.getTransform();
        float armorLeft;
        // Draw Front Side Armor
        GeneralPath fsOutline = new GeneralPath();
        fsOutline.moveTo( 1, 0);
        fsOutline.lineTo(14, 0);
        fsOutline.lineTo(11, 5);
        fsOutline.lineTo( 4, 5);
        fsOutline.lineTo( 1, 0);

        g.setColor(Color.darkGray);
        g.draw(fsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("FS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fsOutline);

        // Draw Rotor Armor
        GeneralPath rOutline = new GeneralPath();
        rOutline.moveTo( 4,  5);
        rOutline.lineTo(11,  5);
        rOutline.lineTo(11, 16);
        rOutline.lineTo( 4, 16);
        rOutline.lineTo( 4,  5);

        g.setColor(Color.darkGray);
        g.draw(rOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("R"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rOutline);
        
        // Draw Left Side Armor
        GeneralPath lsOutline = new GeneralPath();
        lsOutline.moveTo( 1,  0);
        lsOutline.lineTo( 4,  5);
        lsOutline.lineTo( 4, 16);
        lsOutline.lineTo( 1, 20);
        lsOutline.lineTo( 1,  0);

        g.setColor(Color.darkGray);
        g.draw(lsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("LS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(lsOutline);
        
        // Draw Right Side Armor
        GeneralPath rsOutline = new GeneralPath();
        rsOutline.moveTo( 14,  0);
        rsOutline.lineTo( 11,  5);
        rsOutline.lineTo( 11, 16);
        rsOutline.lineTo( 14, 20);
        rsOutline.lineTo( 14,  0);
        
        g.setColor(Color.darkGray);
        g.draw(rsOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("RS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rsOutline); 
        
        // Draw Aft Side Armor
        GeneralPath asOutline = new GeneralPath();
        asOutline.moveTo( 4, 16);
        asOutline.lineTo(11, 16);
        asOutline.lineTo(14, 20);
        asOutline.lineTo( 1, 20);
        asOutline.lineTo( 4, 16);
        
        g.setColor(Color.darkGray);
        g.draw(asOutline);
        armorLeft = data.myUnit.percentArmorLeft(MUUnitInfo.indexForSection("AS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(asOutline);
        

        // Draw Internals
        // **************
        xform.translate(20, 0);
        g.setTransform(xform);

        // Draw Front Side Internals
        g.setColor(Color.darkGray);
        g.draw(fsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("FS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(fsOutline);

        // Draw Rotor Internals
        g.setColor(Color.darkGray);
        g.draw(rOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("R"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rOutline);
        
        // Draw Left Side Internals
        g.setColor(Color.darkGray);
        g.draw(lsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("LS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(lsOutline);	
        
        // Draw Right Side Internals
        g.setColor(Color.darkGray);
        g.draw(rsOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("RS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(rsOutline);
        
        // Draw Aft Side Internals
        g.setColor(Color.darkGray);
        g.draw(asOutline);
        armorLeft = data.myUnit.percentInternalLeft(MUUnitInfo.indexForSection("AS"));
        g.setColor(MUUnitInfo.colorForPercent(armorLeft, armorTransparency));
        g.fill(asOutline);	
    }
    
    public void setHexHeight (int iVal)
    {
    	if (iMapNum == 1)
    		prefs.hexHeight2 = iVal;
    	else
    		prefs.hexHeight = iVal;
    }
    
    public int getHexHeight ()
    {
    	if (iMapNum == 1)
    		return prefs.hexHeight2;
    	
    	return prefs.hexHeight;
    }
}
