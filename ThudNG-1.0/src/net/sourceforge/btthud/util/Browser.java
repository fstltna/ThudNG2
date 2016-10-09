package net.sourceforge.btthud.util;

import java.awt.Desktop;
import java.net.URI;

public class Browser {
	public static void bringUpURL (URI myURL)
	{
		try
		{
			Desktop.getDesktop ().browse(myURL);
		}
		catch (Exception ex)
		{
			
		}
	}
}
