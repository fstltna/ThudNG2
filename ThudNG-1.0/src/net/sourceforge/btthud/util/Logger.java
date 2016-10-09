package net.sourceforge.btthud.util;

import java.io.*;
import java.util.Calendar;

public class Logger {
	protected static FileOutputStream fos = null;
	protected static OutputStreamWriter out = null;
	protected static String sBaseName = "ThudLog.txt";
	protected String sTitle;
	protected static boolean bNoLog = false;
	
	public Logger (String title)
	{
		sTitle = title;
	}
	
	
	public String getTitle() {
		return sTitle;
	}

	public void setTitle(String sTitle) {
		this.sTitle = sTitle;
	}
	
	public static String getBaseName ()
	{
		return sBaseName;
	}
	
	public static void setBaseName (String name)
	{
		sBaseName = name;
	}
	
	public void Log (String sMsg)
	{
		outLog (sTitle + ": " + sMsg);
	}
	
	public void LogStackTrace (Exception ex)
	{
		StackTraceElement myElements [];
		
		Log ("Exception: " + ex.getMessage ());
		Log ("StackTrace");
		
		myElements = ex.getStackTrace ();
		
		int		i, len;
		
		len = myElements.length;
		for (i = 0; i < len; ++i)
		{
			outLog (myElements [i].toString ());
		}
	}
	
	public static boolean getNoLog ()
	{
		return bNoLog;
	}
	
	public static void setNoLog (boolean bVal)
	{
		bNoLog = bVal;
	}
	
	protected static void outLog (String sMsg)
	{
		if (bNoLog)
			return;
		
		if (fos == null)
		{
			try
			{
				String		myHomeDir = System.getProperty("user.home");
				
				File myFile = new File (myHomeDir + "/ThudLog.txt");
				
				if (myFile.exists ())
					myFile.delete ();
				
				fos = new FileOutputStream(myFile);
				out = new OutputStreamWriter(fos, "US-ASCII"); 
			}
			catch (Exception ex)
			{
				System.out.println (ex.getMessage ());
				ex.printStackTrace ();
			}
		}

		Calendar rightNow = Calendar.getInstance ();
		int year = rightNow.get (Calendar.YEAR);
		int month = rightNow.get (Calendar.MONTH) + 1;
		int mday = rightNow.get (Calendar.DAY_OF_MONTH);
		int hour = rightNow.get (Calendar.HOUR);
		int min = rightNow.get (Calendar.MINUTE);
		int sec = rightNow.get (Calendar.SECOND);
		
		String sTS;
		
		sTS = String.format("%1$4d%2$02d%3$02d%4$02d%5$02d%6$02d", year, month, mday, hour, min, sec);
		
		System.out.println (sMsg);
		if (out != null)
		{
			try
			{
				out.write (sTS + "\r\n");
				out.write (sMsg + "\r\n");
			}
			catch (Exception ex)
			{
				System.out.println (ex.getMessage ());
				ex.printStackTrace ();	
			}
		}
	}
}
