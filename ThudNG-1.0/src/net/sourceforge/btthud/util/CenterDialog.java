package net.sourceforge.btthud.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JDialog;

public class CenterDialog {
	public static void centerScreen (JDialog myDialog)
	{
		Dimension dim = myDialog.getToolkit().getScreenSize();
		Rectangle abounds = myDialog.getBounds();
		
		myDialog.setLocation(
				(dim.width - abounds.width) / 2,
				(dim.height - abounds.height) / 2);
		
		myDialog.requestFocus();
	}
}
