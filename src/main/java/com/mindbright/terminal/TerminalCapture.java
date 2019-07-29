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

package com.mindbright.terminal;

import java.io.OutputStream;
import java.io.IOException;

public final class TerminalCapture implements TerminalOutputListener {

    private OutputStream captureTarget;
    private Terminal     terminal;

    public TerminalCapture(OutputStream captureTarget) {
	this.captureTarget = captureTarget;
    }

    public void startCapture(Terminal terminal) {
	this.terminal = terminal;
	terminal.addOutputListener(this);
    }

    public void endCapture() {
	terminal.removeOutputListener(this);
    }

    public OutputStream getTarget() {
	return captureTarget;
    }

    public void write(char c) {
	write(new byte[] { (byte)c }, 0, 1);
    }

    public void write(char[] c, int off, int len) {
	String str   = new String(c, off, len);
	byte[] bytes = str.getBytes();
	write(bytes, 0, bytes.length);
    }

    public void write(byte[] c, int off, int len) {
	try {
	    captureTarget.write(c, off, len);
	} catch (IOException e) {
	    // !!! TODO report this to someone...
	}
    }

}
