package net.sourceforge.btthud.engine;

import java.io.IOException;
import java.net.UnknownHostException;

import net.sourceforge.btthud.data.MUHost;
import net.sourceforge.btthud.engine.commands.Command;
import net.sourceforge.btthud.ui.Thud;
import net.sourceforge.btthud.util.Logger;

public class MUConnectionBase {
	public boolean connected = false;

	protected Thud errorHandler = null;
	protected boolean go = true;
	protected MUParse parse = null;
	protected Logger myLogger;
	
	public MUConnectionBase (Thud errorHandler, MUParse parse)
           throws UnknownHostException, IOException
    {
		myLogger = new Logger ("MUConnection");
		
		this.errorHandler = errorHandler;
		this.parse = parse;
		this.connected = true;
	}
	
	protected void sendLine (String command) throws IOException
	{
	}
	
	public void sendCommand (final Command command) throws IOException
	{
		if (command.isEmpty())
			return;

		if (!command.toString ().startsWith("connect"))
			myLogger.Log("sendCommand :" + command.toString () + ":");
		
		sendLine (command.toString());
	}

	public void pleaseStop ()
	{
		go = false;
	}
}
