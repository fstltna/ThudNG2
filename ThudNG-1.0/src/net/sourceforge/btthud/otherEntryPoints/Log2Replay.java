package net.sourceforge.btthud.otherEntryPoints;

import net.sourceforge.btthud.util.*;

public class Log2Replay {
	public static void main(String[] args) {
		String sInputName = "D:/BTMUX/ThudLogWEngineCrits.txt";
		String sOutputName = "D:/BTMUX/ThudReplay.txt";
		
		Logger.setBaseName("ThudLog2Replay");
		
		ParseThudLogForReplay.parseLogToReplay(sInputName, sOutputName);
	}
}
