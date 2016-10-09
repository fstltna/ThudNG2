//
//  MUConnection.java
//  Thud
//
//  Created by asp on Fri Nov 16 2001.
//  Copyright (c) 2001-2007 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.engine;

import net.sourceforge.btthud.ui.Thud;
import net.sourceforge.btthud.data.MUHost;

import net.sourceforge.btthud.util.Logger;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * This class is for handling the basic connection between the HUD and the MUX. 
 * It offers limited support for TELNET (RFC 854), mostly for the purpose of
 * ignoring TELNET option negotiation.
 *
 * TODO: Implement the RFC 854 support.
 *
 * @author Anthony Parker
 */
public class MUConnection extends MUConnectionBase implements Runnable {
	protected Socket conn;
	protected BufferedInputStream in;
	protected BufferedOutputStream out;

	protected BufferedReader rd;
	protected BufferedWriter wr;

	protected Thread connThread = null;
	
	/**
	 * Creates a new MUConnection object, connects to the host, and starts
	 * recieving data and storing it.
	 *
	 * @param host The IP address or name of the host we're connecting to.
	 * @param port The port of the host we're connecting to.
	 * @see check
	 * @see endConnection
	 */
	public MUConnection (MUHost host, Thud errorHandler, MUParse parse)
	        throws UnknownHostException, IOException
	{
		super (errorHandler, parse);
		
		myLogger = new Logger ("MUConnection");
		
		this.errorHandler = errorHandler;
		this.parse = parse;

		conn = new Socket (host.getHost(), host.getPort());

		in = new BufferedInputStream (conn.getInputStream());
		out = new BufferedOutputStream (conn.getOutputStream());

		rd = new BufferedReader (new InputStreamReader (in, "US-ASCII"));
		wr = new BufferedWriter (new OutputStreamWriter (out, "US-ASCII"));

		this.connected = true;
		start();
	}

	/**
	 * Start the MUConnection thread.
	 */
	public void start () {
		if (connThread == null) {
			connThread = new Thread (this, "MUConnection");
			connThread.start();
		}
	}

	/**
	 * Write line to output.
	 *
	 * @param command The string which holds the command we want to run.
	 */

	protected void sendLine (String command) throws IOException {
		try {
			wr.write(command);
			wr.write("\r\n");
			wr.flush();
		} catch (SocketException e) {
			// If we get a socket exception - disconnect.
			// This is kinda hacked up - ideally the thread
			// communication would work a little better.
			// But, it prevents a bug where if the remote
			// connection is closed, THUD goes to 100% cpu and
			// throws SocketExceptions all the livelong day :)
			System.err.println("Lost connection - aborting");
			errorHandler.doStop();
			errorHandler.stopConnection();
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	/**
	 * Reads lines from input.
	 */
	public void run () {
		while (go) {
			try {
				// TODO: readLine() isn't smart enough, because
				// we need to parse TELNET sequences (IACs) at
				// the byte stream level.
				//
				// It also might be appropriate to parse ANSI
				// escapes at the byte stream level, but we're
				// probably OK on that count, since ANSI
				// sequences are all encoded within the bounds
				// of the US-ASCII charset.
				parse.parseLine(rd.readLine());
			} catch (IOException e) {
				errorHandler.stopConnection();
			} catch (Exception e) {
				System.err.println("Error: connection: " + e);
			}
		}
	}

	public void pleaseStop () {
		try {
			conn.close(); // close the socket
			this.connected = false;
		} catch (Exception e) {
			System.err.println("Error: closeSocket: " + e);
		}

		go = false;
	}
}
