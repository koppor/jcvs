/******************************************************************************
 *
 * Copyright (c) 1999-2001 AppGate AB. All Rights Reserved.
 * 
 * This file contains Original Code and/or Modifications of Original Code as
 * defined in and that are subject to the MindTerm Public Source License,
 * Version 1.3, (the 'License'). You may not use this file except in compliance
 * with the License.
 * 
 * You should have received a copy of the MindTerm Public Source License
 * along with this software; see the file LICENSE.  If not, write to
 * AppGate AB, Stora Badhusgatan 18-20, 41121 Goteborg, SWEDEN
 *
 *****************************************************************************/

package com.mindbright.net.ftp;

import java.util.Hashtable;
import java.util.StringTokenizer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.EOFException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Date;
import java.text.SimpleDateFormat;

public class FTPServer implements Runnable {
    private Thread myThread;
    private String identity;
    private String user;
    private String password;

    private boolean keepRunning;

    private String type;

    protected InetAddress  localHost;
    protected Socket       dataSocket;
    protected ServerSocket dataPasvAccept;
    protected int[]        dataPortAddr;
    protected boolean      dataConnected;
    protected InputStream  dataIn;
    protected OutputStream dataOut;

    private FTPServerEventHandler eventHandler;
    private BufferedReader        cmdInput;
    private OutputStream          cmdOutput;
    private boolean               needPassword;

    public final static int CMD_UNKN = -1;
    public final static int CMD_USER = 0;
    public final static int CMD_PASS = 1;
    public final static int CMD_ACCT = 2;
    public final static int CMD_CWD  = 3;
    public final static int CMD_CDUP = 4;
    public final static int CMD_SMNT = 5;
    public final static int CMD_QUIT = 6;
    public final static int CMD_REIN = 7;
    public final static int CMD_PORT = 8;
    public final static int CMD_PASV = 9;
    public final static int CMD_TYPE = 10;
    public final static int CMD_STRU = 11;
    public final static int CMD_MODE = 12;
    public final static int CMD_RETR = 13;
    public final static int CMD_STOR = 14;
    public final static int CMD_STOU = 15;
    public final static int CMD_APPE = 16;
    public final static int CMD_ALLO = 17;
    public final static int CMD_REST = 18;
    public final static int CMD_RNFR = 19;
    public final static int CMD_RNTO = 20;
    public final static int CMD_ABOR = 21;
    public final static int CMD_DELE = 22;
    public final static int CMD_RMD  = 23;
    public final static int CMD_MKD  = 24;
    public final static int CMD_PWD  = 25;
    public final static int CMD_LIST = 26;
    public final static int CMD_NLST = 27;
    public final static int CMD_SITE = 28;
    public final static int CMD_SYST = 29;
    public final static int CMD_STAT = 30;
    public final static int CMD_HELP = 31;
    public final static int CMD_NOOP = 32;
    public final static int CMD_FEAT = 33;
    public final static int CMD_MDTM = 34;
    public final static int CMD_SIZE = 35;

    public static Hashtable commands;

    static {
	commands = new Hashtable();
	commands.put("USER", new Integer(CMD_USER));
	commands.put("PASS", new Integer(CMD_PASS));
	commands.put("ACCT", new Integer(CMD_ACCT));
	commands.put("CWD",  new Integer(CMD_CWD));
	commands.put("CDUP", new Integer(CMD_CDUP));
	commands.put("SMNT", new Integer(CMD_SMNT));
	commands.put("QUIT", new Integer(CMD_QUIT));
	commands.put("REIN", new Integer(CMD_REIN));
	commands.put("PORT", new Integer(CMD_PORT));
	commands.put("PASV", new Integer(CMD_PASV));
	commands.put("TYPE", new Integer(CMD_TYPE));
	commands.put("STRU", new Integer(CMD_STRU));
	commands.put("MODE", new Integer(CMD_MODE));
	commands.put("RETR", new Integer(CMD_RETR));
	commands.put("STOR", new Integer(CMD_STOR));
	commands.put("STOU", new Integer(CMD_STOU));
	commands.put("APPE", new Integer(CMD_APPE));
	commands.put("ALLO", new Integer(CMD_ALLO));
	commands.put("REST", new Integer(CMD_REST));
	commands.put("RNFR", new Integer(CMD_RNFR));
	commands.put("RNTO", new Integer(CMD_RNTO));
	commands.put("ABOR", new Integer(CMD_ABOR));
	commands.put("DELE", new Integer(CMD_DELE));
	commands.put("RMD",  new Integer(CMD_RMD));
	commands.put("MKD",  new Integer(CMD_MKD));
	commands.put("XPWD", new Integer(CMD_PWD));
	commands.put("PWD",  new Integer(CMD_PWD));
	commands.put("LIST", new Integer(CMD_LIST));
	commands.put("NLST", new Integer(CMD_NLST));
	commands.put("SITE", new Integer(CMD_SITE));
	commands.put("SYST", new Integer(CMD_SYST));
	commands.put("STAT", new Integer(CMD_STAT));
	commands.put("HELP", new Integer(CMD_HELP));
	commands.put("NOOP", new Integer(CMD_NOOP));
	commands.put("FEAT", new Integer(CMD_FEAT));
	commands.put("MDTM", new Integer(CMD_MDTM));
	commands.put("SIZE", new Integer(CMD_SIZE));
    }

    public FTPServer(String identity, FTPServerEventHandler eventHandler,
		     InputStream cmdInput, OutputStream cmdOutput,
		     boolean needPassword) {
	this(identity, eventHandler, null, cmdInput, cmdOutput, needPassword);
    }

    public FTPServer(String identity, FTPServerEventHandler eventHandler,
		     InetAddress localHost,
		     InputStream cmdInput, OutputStream cmdOutput,
		     boolean needPassword) {
	this.identity      = identity;
	this.eventHandler  = eventHandler;
	this.localHost     = localHost;
	this.cmdInput      = new BufferedReader(new InputStreamReader(cmdInput));
	this.cmdOutput     = cmdOutput;
	this.needPassword  = needPassword;
	this.dataConnected = false;

	if(this.localHost == null) {
	    try {
		this.localHost = InetAddress.getByName("0.0.0.0");
	    } catch (UnknownHostException e) {
		throw new Error("Error in FTPServer: " + e);
	    }
	}

	this.type = "A";

	this.keepRunning = true;

	this.myThread = new Thread(this, "FTPServer_" + identity);
	this.myThread.start();
    }

    public void run() {
	try {
	    netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
	} catch (netscape.security.ForbiddenTargetException e) {
	    // !!!
	}

	try {
	    String[] argv = new String[2];
	    int      code;

	    reply(220, identity);

	    do {
		readLogin();
	    } while(!eventHandler.login(user, password));

	    reply(230, "User " + user + " logged in.");

	    while(keepRunning) {
		code = readCommand(argv);
		try {
		    switch(code) {
		    case CMD_CDUP:
			argv[1] = "..";
		    case CMD_CWD:
			eventHandler.changeDirectory(argv[1]);
			reply(250, "CWD command successful.");
			break;
		    case CMD_QUIT:
			eventHandler.quit();
			// !!! TODO generate some stats ???
			reply(221, "Goodbye.");
			keepRunning = false;
			break;
		    case CMD_PORT:
			dataPortAddr = dataPort(argv[1]);
			reply(200, "PORT command successful.");
			break;
		    case CMD_PASV:
			String hostAndPort = dataPassive();
			reply(227, "Entering passive mode (" +
			      hostAndPort + ")");
			break;
		    case CMD_TYPE:
			if("A".equals(argv[1]) || "I".equals(argv[1])) {
			    type = argv[1];
			    reply(200, "Type set to " + argv[1] + ".");
			} else {
			    reply(504, "Type " + argv[1] + " not implemented.");
			}
			break;
		    case CMD_RETR: {
			checkArgAndPlainFile(argv);
			OutputStream data = getDataOutput();
			dataStart(argv[1]);
			eventHandler.retrieve(argv[1], data, type.equals("I"));
			dataComplete();
			break;
		    }
		    case CMD_STOR: {
			InputStream data = getDataInput();
			dataStart(argv[1]);
			eventHandler.store(argv[1], data, type.equals("I"));
			dataComplete();
			break;
		    }
		    case CMD_RNFR:
			eventHandler.renameFrom(argv[1]);
			reply(350, "File exists, ready for destination name.");
			break;
		    case CMD_RNTO:
			eventHandler.renameTo(argv[1]);
			reply(250, "RNTO command successful.");
			break;
		    case CMD_ABOR:
			eventHandler.abort();
			dataReset();
			reply(225, "ABOR command successful.");
			break;
		    case CMD_DELE:
			eventHandler.delete(argv[1]);
			reply(250, "DELE command successful.");
			break;
		    case CMD_RMD:
			eventHandler.rmdir(argv[1]);
			reply(250, "RMD command successful.");
			break;
		    case CMD_MKD:
			eventHandler.mkdir(argv[1]);
			reply(257, "\"" + argv[1] +
			      "\" new directory created.");
			break;
		    case CMD_PWD:
			reply(257, "\"" + eventHandler.pwd() +
			      "\" is current directory.");
			break;
		    case CMD_LIST: {
			OutputStream data = getDataOutput();
			dataStart("list" + (argv[1] != null ? " " + argv[1] :
					    ""));
			eventHandler.list(argv[1], data);
			dataComplete();
			break;
		    }
		    case CMD_NLST: {
			OutputStream data = getDataOutput();
			dataStart("nlst" + (argv[1] != null ? argv[1] : ""));
			eventHandler.nameList(argv[1], data);
			dataComplete();
			break;
		    }
		    case CMD_SYST:
			reply(215, eventHandler.system());
			break;
		    case CMD_NOOP:
			reply(200, "NOOP command successful.");
			break;
		    case CMD_FEAT:
			reply(211, "Extensions supported:\nSIZE\nMDTM\nEND");
			break;
		    case CMD_MDTM:
			checkArgAndPlainFile(argv);
			long modt = eventHandler.modTime(argv[1]);
			SimpleDateFormat df =
			    new SimpleDateFormat("yyyyMMddhhmmss");
			reply(213, df.format(new Date(modt)));
			break;
		    case CMD_SIZE:
			checkArgAndPlainFile(argv);
			long size = eventHandler.size(argv[1]);
			reply(213, String.valueOf(size));
			break;
		    case CMD_HELP:
		    case CMD_STAT:
		    case CMD_SITE:
		    case CMD_USER:
		    case CMD_PASS:
		    case CMD_ACCT:
		    case CMD_SMNT:
		    case CMD_REIN:
		    case CMD_STRU:
		    case CMD_MODE:
		    case CMD_STOU:
		    case CMD_APPE:
		    case CMD_ALLO:
		    case CMD_REST:
			reply(502, "'" + argv[0] +
			      "': command not implemented");
			break;
		    case CMD_UNKN:
			reply(500, "'" + argv[0] + "': command not understood");
			break;
		    }
		} catch (FTPException e) {
		    if(dataConnected) {
			dataReset();
		    }
		    reply(e.getCode(), e.getMessage());
		}
	    }

	} catch (IOException e) {
	    // !!! TODO eventhandler.error ...
	} finally {
	    try { cmdOutput.close(); }
	    catch (IOException e) { /* don't care */ }
	    try { cmdInput.close(); }
	    catch (IOException e) { /* don't care */ }

	    // !!! TODO other clean up ???
	}
    }

    private void checkArgAndPlainFile(String[] argv)
	throws FTPException
    {
	if(argv.length < 2 || argv[1] == null) {
	    throw new FTPException(500, "'" + argv[0] +
				   "': command not understood");
	}
	if(!eventHandler.isPlainFile(argv[1])) {
	    throw new FTPException(550, argv[1] + ": Not a plain file.");
	}
    }

    private int readCommand(String[] argv) throws IOException {
	String cmdLine = cmdInput.readLine();
	if(cmdLine == null) {
	    // !!! TODO
	    throw new EOFException();
	}

	int i = cmdLine.indexOf(' ');
	if(i != -1) {
	    argv[0] = cmdLine.substring(0, i);
	    argv[1] = cmdLine.substring(i + 1);
	} else {
	    argv[0] = cmdLine.trim();
	    argv[1] = null;
	}

	argv[0] = argv[0].toUpperCase();
	Integer code = (Integer)commands.get(argv[0]);

	if(code == null) {
	    return -1;
	}
	return code.intValue();
    }

    public void reply(int code, String text) throws IOException {
	String msg;
	if(text.indexOf('\n') != -1) {
	    msg = code + "-";
	    StringTokenizer st = new StringTokenizer(text, "\r\n");
	    while(st.hasMoreTokens()) {
		String line = st.nextToken();
		if(st.hasMoreTokens()) {
		    msg += " " + line + "\r\n";
		} else {
		    msg += code + " " + line;
		}
	    }
	} else {
	    msg = code + " " + text + "\r\n";
	}
	cmdOutput.write(msg.getBytes());
    }

    public void readLogin() throws IOException {
	user     = null;
	password = null;
	while(user == null || password == null) {
	    String[] argv = new String[2];
	    int code = readCommand(argv);
	    if(code == CMD_USER) {
		user = argv[1];
		if(user != null) {
		    if(needPassword) {
			reply(331, "Password required for " + user + ".");
			continue;
		    } else {
			return;
		    }
		}
	    } else if(code == CMD_PASS && user != null) {
		password = argv[1];
	    }
	    if(password == null) {
		reply(530, "Please login with USER and PASS.");
	    }
	}
    }

    protected int[] dataPort(String arg) throws FTPException {
	dataReset();
	StringTokenizer st = new StringTokenizer(arg, ",");
	int i = 0;
	int[] d = new int[6];
	while(i < 6 && st.hasMoreTokens()) {
	    try {
		d[i++] = Integer.parseInt(st.nextToken());
	    } catch (NumberFormatException e) {
		throw new FTPException(500, "'PORT " + arg +
				       "': command not understood");
	    }
	}
	if(i < 6) {
	    throw new FTPException(500, "'PORT " + arg +
				   "': command not understood");
	}
	return d;
    }

    protected String dataPassive() throws FTPException {
	dataReset();

	try {
	    dataPasvAccept = new ServerSocket(0, 16, localHost);
	    dataPasvAccept.setSoTimeout(30000);
	} catch (Exception e) {
	    dataReset();
	    throw new FTPException(500, "Error, unable to proceede: " +
				   e.getMessage());
	}

	byte[] localAddrArr = null;
	localAddrArr        = localHost.getAddress();
	// !!! TODO: want to be able to get actual connected adress
	if((localAddrArr[0] & 0xff) == 0) {
	    try {
		localAddrArr = InetAddress.getLocalHost().getAddress();
	    } catch (UnknownHostException e) {
		throw new Error("Error in FTPServer: " + e);
	    }
	}
	int a1 = localAddrArr[0] & 0xff;
	int a2 = localAddrArr[1] & 0xff;
	int a3 = localAddrArr[2] & 0xff;
	int a4 = localAddrArr[3] & 0xff;

	int port = dataPasvAccept.getLocalPort();
	int p1 = (port >>> 8) & 0xff;
	int p2 = port & 0xff;

	return a1 + "," + a2 + "," + a3 + "," + a4 + "," + p1 + "," + p2;
    }

    protected OutputStream getDataOutput() throws FTPException {
	dataConnect();
	if(dataSocket == null) {
	    throw new FTPException(550, "Error when making data connection.");
	}
	try {
	    dataOut = dataSocket.getOutputStream();
	    return dataOut;
	} catch (IOException e) {
	    throw new FTPException(425, "Can't build data connection: " +
				   e.getMessage());
	}
    }

    protected InputStream getDataInput() throws FTPException {
	dataConnect();
	try {
	    dataIn = dataSocket.getInputStream();
	    return dataIn;
	} catch (IOException e) {
	    throw new FTPException(425, "Can't build data connection: " +
				   e.getMessage());
	}
    }

    protected void dataConnect() throws FTPException {
	try {
	    if(dataPasvAccept != null) {
		dataSocket = dataPasvAccept.accept();
	    } else if(dataPortAddr != null) {
		String toHost = dataPortAddr[0] + "." + dataPortAddr[1] + "." +
		    dataPortAddr[2] + "." + dataPortAddr[3];
		int    toPort = (dataPortAddr[4] << 8) | dataPortAddr[5];
		dataSocket = new Socket(toHost, toPort);
	    }
	    dataConnected = true;
	} catch (IOException e) {
	    dataReset();
	    throw new FTPException(425, "Can't build data connection: " +
				   e.getMessage());
	}
    }

    protected void dataStart(String obj) throws IOException {
	reply(150, "Opening " +
	      (type.equals("A") ? "ASCII" : "BINARY") +
	      " mode data connection for " + obj + ".");
    }

    protected void dataComplete() throws IOException {
	reply(226, "Transfer complete.");
	dataReset();
    }

    protected void dataReset() {
	if(dataPasvAccept != null) {
	    try { dataPasvAccept.close(); }
	    catch (Exception e) { /* don't care */ }
	    dataPasvAccept = null;
	}
	dataPortAddr = null;
	if(dataSocket != null) {
	    try { if(dataOut != null) dataOut.close();
	    } catch (IOException e) { /* don't care */ }
	    try { if(dataIn != null) dataIn.close();
	    } catch (IOException e) { /* don't care */ }
	    try { dataSocket.close(); }
	    catch (Exception e) { /* don't care */ }
	}
	dataSocket    = null;
	dataOut       = null;
	dataIn        = null;
	dataConnected = false;
    }

}
