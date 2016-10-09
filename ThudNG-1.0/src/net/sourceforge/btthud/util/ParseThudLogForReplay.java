package net.sourceforge.btthud.util;

import java.io.*;

public class ParseThudLogForReplay {
	public static void extractReplay (String sReplayName, int iStartLine, int iEndLine, String sOutReplay)
	{
		File	myInputFile = new File (sReplayName);
		File	myOutputFile = new File (sOutReplay);
		boolean	bLastIsSend = false;
		boolean bPauseHUD = false;
		
		int		iCurrentLine = 0;
		int		iWLOASFlag = 0;
		
		Logger	myLogger = new Logger ("Parse");
		
		// 0 - not found
		// 1 - PAUSEHUD found
		// 2 - WLOAS found
		// 3 - Done
		
		
		try
		{
	        BufferedReader in = new BufferedReader(new FileReader(myInputFile));
	        BufferedWriter out = new BufferedWriter (new FileWriter (myOutputFile));
	        
	        myLogger.Log ("PARSE: 001 :" + iWLOASFlag + ":");
	        
	        String sLine;
	        iCurrentLine = -1;
	        while ((sLine = in.readLine()) != null)
	        {
	        	iCurrentLine ++;
	        	myLogger.Log ("PARSE: 002 :" + iWLOASFlag + ":" + iCurrentLine + ":");
	        	myLogger.Log ("PARSE: 003 :" + sLine + ":");
		        
	        	switch (iWLOASFlag)
	        	{
	        	case 0 :     // Not found
	        		if (sLine.contains ("_PAUSEHUD_"))
	        		{
	    	        	myLogger.Log ("PARSE: 004 :Found Pausehud:");
	    	        	
	        			iWLOASFlag = 1;
	        		}
	        		break;
	        	case 1 :     // PAUSEHUD found
	        		if (sLine.startsWith ("#HUD"))
	        		{
	    	        	myLogger.Log ("PARSE: 005 :Found #HUD:");

	        			if (sLine.contains (":WL:") ||
	        				sLine.contains (":OAS:") ||
	        				sLine.contains ("hudinfo version"))
	        			{
	        				iWLOASFlag = 2;
	        				
	        				out.write ("_PAUSEHUD_" + "\n");
	        				out.write (sLine + "\n");
	        			}
	        		}
	        		else
	        		{
	    	        	myLogger.Log ("PARSE: 006 :Found #HUD:");

	        			iWLOASFlag = 0;
	        		}
	        		break;
	        	case 2 :     // WLOAS found
	        		if (!sLine.startsWith("#HUD:"))
	        		{
	    	        	myLogger.Log ("PARSE: 007 :Found WLOAS:");

	        			iWLOASFlag = 3;
	        		}
	        		else
	        		{
	    	        	myLogger.Log ("PARSE: 008 :Found WLOAS:");

	        			if (sLine.contains (":WL:") ||
	        				sLine.contains (":OAS:") ||
	        				sLine.contains ("hudinfo version"))
	        				out.write (sLine + "\n");
	        		}
	        		break;
	        	case 3 :     // Done
	        	default :
	        		break;
	        	}
	        	
	        	if (iCurrentLine >= iStartLine &&
	        		iCurrentLine <= iEndLine)
	        	{
	        		if (sLine.contains("_PAUSEHUD_"))
	        			bPauseHUD = true;
	        		
	        		if (!bPauseHUD)
	        			continue;
	        		
	        		out.write (sLine + "\n");
	        	}
	        	
	        	if (iCurrentLine > iEndLine)
	        		break;
	        }
	        in.close();
	        out.close ();
		}
		catch (Exception ex)
		{
			System.out.println("Exception :" + ex.getMessage ());
			
			return;
		}
	}
	
	public static void parseLogToReplay (String sLogName, String sReplayName)
	{
		File	myInputFile = new File (sLogName);
		File	myOutputFile = new File (sReplayName);
		Logger	myLogger = new Logger ("Log2Replay");
		boolean	bLastIsSend = false;
		
		try
		{
			System.out.println ("Openning Log File");
			
	        BufferedReader in = new BufferedReader(new FileReader(myInputFile));
	        BufferedWriter out = new BufferedWriter (new FileWriter (myOutputFile));
	        
	        String sLine;
	        while ((sLine = in.readLine()) != null)
	        {
	            if (sLine.contains("parseLine :"))
	            {
	            	String	sWork;
	            	int		iIdx;
	            	int		iLen;
	            	
	            	iIdx = sLine.indexOf("parseLine :");
	            	
	            	sWork = sLine.substring(iIdx + 11);
	            	iLen = sWork.length ();
	            	
	            	if (sWork.charAt(iLen - 1) == ':')
	            		sWork = sWork.substring(0, iLen - 1);
	            	
	            	out.write(sWork + "\n");
	            	
	            	bLastIsSend = false;
	            }
	            
	            if (sLine.contains("sendCommand :hudinfo"))
	            {            	
	            	if (bLastIsSend)
	            		continue;
	            	
	            	bLastIsSend = true;
	            	
	            	out.write("_PAUSEHUD_" + "\n");
	            }
	        }
	        in.close();
	        out.close ();
		}
		catch (Exception ex)
		{
			System.out.println("Exception :" + ex.getMessage ());
			
			return;
		}
	}
}
