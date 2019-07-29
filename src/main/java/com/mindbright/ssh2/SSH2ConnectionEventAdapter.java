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

import java.net.Socket;

/**
 * This class is an adapter for the interface
 * <code>SSH2ConnectionEventHandler</code>.
 *
 * @see SSH2ConnectionEventHandler
 */
public class SSH2ConnectionEventAdapter implements SSH2ConnectionEventHandler
{
    public void channelAdded(SSH2Connection connection, SSH2Channel channel) {
    }
    public void channelDeleted(SSH2Connection connection, SSH2Channel channel) {
    }
    public void channelClosed(SSH2Connection connection, SSH2Channel channel) {
    }

    public void listenerAccept(SSH2Listener listener, Socket fwdSocket) {
	listener.doConnect(fwdSocket);
    }
    public void listenerConnect(SSH2Listener listener, Socket fwdSocket) {
    }

    public void localForwardedConnect(SSH2Connection connection,
				      SSH2Listener listener,
				      SSH2Channel channel) {
    }
    public void localDirectConnect(SSH2Connection connection,
				   SSH2Listener listener,
				   SSH2Channel channel) {
    }
    public void localSessionConnect(SSH2Connection connection,
				    SSH2Channel channel) {
    }
    public void localX11Connect(SSH2Connection connection,
				SSH2Listener listener,
				SSH2Channel channel) {
    }
    public void localChannelOpenFailure(SSH2Connection connection,
					SSH2Channel channel,
					int reasonCode, String reasonText,
					String languageTag) {
    }

    public void remoteForwardedConnect(SSH2Connection connection,
				       String remoteAddr, int remotePort,
				       SSH2Channel channel) {
    }
    public void remoteDirectConnect(SSH2Connection connection,
				    SSH2Channel channel) {
    }
    public void remoteSessionConnect(SSH2Connection connection,
				     String remoteAddr, int remotePort,
				     SSH2Channel channel) {
    }
    public void remoteX11Connect(SSH2Connection connection,
				 SSH2Channel channel) {
    }
    public void remoteChannelOpenFailure(SSH2Connection connection,
					 String channelType,
					 String targetAddr, int targetPort,
					 String originAddr, int originPort,
					 SSH2Exception cause) {
    }

}
