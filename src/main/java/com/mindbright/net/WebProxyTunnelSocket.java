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

package com.mindbright.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.SocketException;

import com.mindbright.util.Base64;

/**
 * Socket that implements web proxy tunnelling (using CONNECT)
 *
 * (described in an Internet Draft (expired Feb. 1999) titled
 * 'Tunneling TCP based protocols through Web proxy servers'
 * by: Ari Luotonen)
 *
 * proxy-authentication is described in RFC2616 and RFC2617
 *
 * @author  Mats Andersson (originally by John Pallister)
 */
public class WebProxyTunnelSocket extends Socket {

    private String proxyHost;
    private int    proxyPort;
    private String targetHost;
    private int    targetPort;

    HttpHeader responseHeader;
    String     serverDesc;

    public HttpHeader getResponseHeader() {
	return responseHeader;
    }

    public String getServerDesc() {
	return serverDesc;
    }

    private WebProxyTunnelSocket(String host, int port, String proxyHost, int proxyPort)
	throws IOException, UnknownHostException
    {
	super(proxyHost, proxyPort);

	this.proxyHost  = proxyHost;
	this.proxyPort  = proxyPort;
	this.targetHost = targetHost;
	this.targetPort = targetPort;
    }

    public static WebProxyTunnelSocket getProxy(String host, int port, String proxyHost, int proxyPort, String protoStr,
						ProxyAuthenticator authenticator, String userAgent)
	throws IOException, UnknownHostException
    {
	WebProxyTunnelSocket proxySocket = new WebProxyTunnelSocket(host, port, proxyHost, proxyPort);
	int status = -1;
	String serverDesc;

	try {
	    InputStream  proxyIn  = proxySocket.getInputStream();
	    OutputStream proxyOut = proxySocket.getOutputStream();
	    HttpHeader   requestHeader = new HttpHeader();

	    if(protoStr == null)
		protoStr = "";

	    requestHeader.setStartLine("CONNECT " + protoStr + host + ":" + port + " HTTP/1.0");
	    requestHeader.setHeaderField("User-Agent", userAgent);
	    requestHeader.setHeaderField("Pragma", "No-Cache");
	    requestHeader.setHeaderField("Proxy-Connection", "Keep-Alive");

	    requestHeader.writeTo(proxyOut);
	    proxySocket.responseHeader = new HttpHeader(proxyIn);

	    serverDesc = proxySocket.responseHeader.getHeaderField("server");

	    // If proxy requires authentication
	    //
	    if(proxySocket.responseHeader.getStatus() == 407 && authenticator != null) {
		String method = proxySocket.responseHeader.getProxyAuthMethod();
		String realm  = proxySocket.responseHeader.getProxyAuthRealm();

		if(realm == null)
		    realm = "";

		if("basic".equalsIgnoreCase(method)) {
		    // OK
		} else if("digest".equalsIgnoreCase(method)) {
		    throw new IOException("We don't support 'Digest' HTTP Authentication");
		} else {
		    throw new IOException("Unknown HTTP Authentication method '" + method + "'");
		}

		proxySocket.close();
		proxySocket = new WebProxyTunnelSocket(host, port, proxyHost, proxyPort);
		proxyIn  = proxySocket.getInputStream();
		proxyOut = proxySocket.getOutputStream();

		String username = authenticator.getProxyUsername("HTTP Proxy", realm);
		String password = authenticator.getProxyPassword("HTTP Proxy", realm);

		requestHeader.setBasicProxyAuth(username, password);
		requestHeader.writeTo(proxyOut);
		proxySocket.responseHeader = new HttpHeader(proxyIn);
	    }

	    status = proxySocket.responseHeader.getStatus();

	} catch (SocketException e) {
	    throw new SocketException("Error communicating with proxy server " +
				      proxyHost + ":" + proxyPort + " (" + e.getMessage()  + ")");
	}

	if((status < 200) || (status > 299))
	    throw new WebProxyException("Proxy tunnel setup failed: " +
					proxySocket.responseHeader.getStartLine());

	proxySocket.serverDesc = serverDesc;

	return proxySocket;
    }

    public String toString() {
	return "WebProxyTunnelSocket[addr=" + getInetAddress() +
	    ",port=" + getPort() +
	    ",localport=" + getLocalPort() + "]";
    }
}
