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

package com.mindbright.ssh2;

import java.io.File;
import java.io.OutputStream;

import com.mindbright.sshcommon.SSHSCP1;

public final class SSH2SCP1Client extends SSH2ConsoleRemote {

    private SSHSCP1 scp1;

    public SSH2SCP1Client(File cwd, SSH2Connection connection,
			  OutputStream stderr, boolean verbose) {
	super(connection, stderr);
	this.scp1 = new SSHSCP1(cwd, this, verbose);
    }

    public SSHSCP1 scp1() {
	return scp1;
    }

}
