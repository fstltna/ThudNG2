package net.sourceforge.btthud.engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import net.sourceforge.btthud.ui.Thud;

public class MUReplayConnection extends MUConnectionBase implements Runnable {
	protected Thread connThread = null;
	protected boolean bWaiting;
	protected BufferedReader in;
	protected String		sSessionKey;
	protected boolean		bFastForward;
	protected int			iFastCount;
	protected boolean 		bPaused;
	
	protected int			iCurrentLine;
	protected int			iMarkedLine;
	
	public boolean isPaused() {
		return bPaused;
	}

	public void setPaused(boolean bPaused) {
		this.bPaused = bPaused;
	}

	public int getCurrentLine() {
		return iCurrentLine;
	}

	public void setCurrentLine(int iCurrentLine) {
		this.iCurrentLine = iCurrentLine;
	}

	public int getMarkedLine() {
		return iMarkedLine;
	}

	public void setMarkedLine(int iMarkedLine) {
		this.iMarkedLine = iMarkedLine;
	}

	public MUReplayConnection (Thud errorHandler, MUParse parse, String sReplayFName)
           throws UnknownHostException, IOException
    {
		super (errorHandler, parse);
		
        in = new BufferedReader(new FileReader(sReplayFName));

		bWaiting = false;
		sSessionKey = null;
		bPaused = false;
		iCurrentLine = 0;
		iMarkedLine = 0;
		
		start ();
	}
	
	protected void start () {
		if (connThread == null) {
			connThread = new Thread (this, "MUReplayConnection");
			connThread.start();
		}
	}
	
	protected void sendLine (String command) throws IOException
	{
		if (command.contains("hudinfo"))
		{
			if (bWaiting)
				bWaiting = false;
		}
	}
	
	/**
	 * Reads lines from input.
	 */
	public void run () {
		String		sLine;
		
		bWaiting = false;
		iCurrentLine = 0;
		iMarkedLine = 0;
		
		while (go) {
			try {
				if (bFastForward)
				{
					if (iFastCount > 10000)
						bFastForward = false;
					
					iFastCount++;
				}
				else
				{
					if (bWaiting || bPaused)
					{
						Thread.sleep(250);
						continue;
					}
				}
				
				iCurrentLine++;
				sLine = in.readLine ();
				myLogger.Log("Line :" + sLine + ":");
				
				if (sLine.contains ("_PAUSEHUD_"))
				{
					bWaiting = true;
					continue;
				}
				
				myLogger.Log("Line :" + sLine + ":");
				
				if (sSessionKey == null && sLine.contains("#HUD:"))
				{
					// parse out the session key
					
					int		iIdx = sLine.indexOf("#HUD:");
					
					myLogger.Log("SESSIONKEY: 001");
					
					String  sWork = sLine.substring (iIdx + 5);
					
					myLogger.Log("SESSIONKEY: 002 :" + sWork + ":");
					
					iIdx = sWork.indexOf (":");
					
					sSessionKey = sWork.substring (0, iIdx);
					
					myLogger.Log("SESSIONKEY: 003 :" + sSessionKey + ":");
					
					parse.sessionKey = sSessionKey;
				}
				
				parse.parseLine(sLine);
			} catch (IOException e) {
				errorHandler.stopConnection();
			} catch (Exception e) {
				myLogger.Log ("Error: connection: " + e);
				myLogger.LogStackTrace (e);
			}
		}
	}
	
	public void fastForward ()
	{
		bFastForward = true;
		iFastCount = 0;
	}
}
