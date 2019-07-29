/*
** Java cvs client library package.
** Copyright (c) 1997-2002 by Timothy Gerard Endres
**
** This program is free software.
**
** You may redistribute it and/or modify it under the terms of the GNU
** Library General Public License (LGPL) as published by the Free Software
** Foundation.
**
** Version 2 of the license should be included with this distribution in
** the file LICENSE.txt, as well as License.html. If the license is not
** included	with this distribution, you may find a copy at the FSF web
** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the Free
** Software Foundation at 59 Temple Place - Suite 330, Boston, MA 02111 USA.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE.
**
*/

package com.ice.cvsc;

/**
 * CVSMode implements the concept of file permissions. In other words,
 * CVSMode objects are used to represent, parse, and otherwise handle
 * CVS 'mode' lines such as 'u=rw,g=r,o=r'.
 *
 * @version $Revision: 2.2 $
 * @author Timothy Gerard Endres, <a href="mailto:time@ice.com">time@ice.com</a>.
 * @see CVSClient
 * @see CVSEntry
 */

public class
CVSMode extends Object
		implements Cloneable
	{
	static public final String		RCS_ID = "$Id: CVSMode.java,v 2.2 2003/07/27 01:08:32 time Exp $";
	static public final String		RCS_REV = "$Revision: 2.2 $";

	public boolean		userRead;
	public boolean		userWrite;
	public boolean		userExecute;

	public boolean		groupRead;
	public boolean		groupWrite;
	public boolean		groupExecute;

	public boolean		otherRead;
	public boolean		otherWrite;
	public boolean		otherExecute;


	public CVSMode()
		{
		super();

		this.userRead = true;
		this.userWrite = true;
		this.userExecute = false;

		this.groupRead = true;
		this.groupWrite = false;
		this.groupExecute = false;

		this.otherRead = true;
		this.otherWrite = false;
		this.otherExecute = false;
		}

	public String
	getModeLine()
		{
		final StringBuffer	result =
			new StringBuffer( "" );

		result.append( "u=" );
		if ( this.userRead ) result.append( "r" );
		if ( this.userWrite ) result.append( "w" );
		if ( this.userExecute ) result.append( "x" );

		result.append( "," );

		result.append( "g=" );
		if ( this.groupRead ) result.append( "r" );
		if ( this.groupWrite ) result.append( "w" );
		if ( this.groupExecute ) result.append( "x" );

		result.append( "," );

		result.append( "o=" );
		if ( this.otherRead ) result.append( "r" );
		if ( this.otherWrite ) result.append( "w" );
		if ( this.otherExecute ) result.append( "x" );

		return result.toString();
		}

	@Override
	public String
	toString()
		{
		return this.getModeLine();
		}
	}




