/*
** Copyright (c) 1998 by Timothy Gerard Endres
** <mailto:time@ice.com>  <http://www.ice.com>
**
** This program is free software.
**
** You may redistribute it and/or modify it under the terms of the GNU
** General Public License as published by the Free Software Foundation.
** Version 2 of the license should be included with this distribution in
** the file LICENSE, as well as License.html. If the license is not
** included	with this distribution, you may find a copy at the FSF web
** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
** Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE.
**
*/

package com.ice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


public class
TempFileManager extends Object
	{
	private static int			tempDocCounter = 0;

	private static String		temnpDirName = null;
	private static String		tempFilePrefix = null;
	private static String		tempFileSuffix = null;

	private static Vector		tempFiles = new Vector();


	public static void
	initialize( final String dirName, final String prefix, final String suffix )
		{
		TempFileManager.temnpDirName = dirName;
		TempFileManager.tempFilePrefix = prefix;
		TempFileManager.tempFileSuffix = suffix;
		}

	public static void
	setTemporaryDirectory( final String dirName )
		{
		TempFileManager.temnpDirName = dirName;
		}

	public static void
	setFilenamePrefix( final String prefix )
		{
		TempFileManager.tempFilePrefix = prefix;
		}

	public static void
	setFilenameSuffix( final String suffix )
		{
		TempFileManager.tempFileSuffix = suffix;
		}

	public static void
	addTemporaryFile( final String filename )
		{
		TempFileManager.tempFiles.addElement( filename );
		}

	public static String
	getTemporaryDirectory()
		{
		return TempFileManager.temnpDirName;
		}

	public static String
	getTemporaryFilename()
		{
		TempFileManager.tempDocCounter++;

		return
			TempFileManager.getTemporaryFilename
				( TempFileManager.tempFileSuffix );
		}

	public static String
	getTemporaryFilename( final String suffix )
		{
		TempFileManager.tempDocCounter++;

		final String tempFileName =
			TempFileManager.temnpDirName
			+ File.separator
			+ TempFileManager.tempFilePrefix
			+ "-" + TempFileManager.tempDocCounter
			+ suffix;

		return tempFileName;
		}

	public static void
	clearTemporaryFiles()
		{
		int count = 0;
		final int numFiles = TempFileManager.tempFiles.size();

		for ( int idx = 0 ; idx < numFiles ; ++idx )
			{
			final String fileName =
				(String) TempFileManager.tempFiles.elementAt( idx );

			final File f = new File( fileName );

			if ( f.exists() && f.isFile() )
				{
				f.delete();
				count++;
				}
			}

		System.err.println
			( "Deleted " + count + " temporary documents." );
		}

	public static void
	writeTemporaryFile( final InputStream source, final String tempFileName )
		throws IOException
		{
		final FileOutputStream out =
			new FileOutputStream( tempFileName );

		final byte[] buf = new byte[ 32 * 1024 ];
		for ( ; ; )
			{
			final int cnt = source.read( buf, 0, buf.length );
			if ( cnt == -1 )
				break;

			out.write( buf, 0, cnt );
			}

		out.close();

		TempFileManager.addTemporaryFile( tempFileName );
		}

	}

