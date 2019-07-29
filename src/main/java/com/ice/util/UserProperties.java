/*
** Copyright (c) 1997 by Tim Endres
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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;


/**
 * The UserProperties class. This class is used to extend
 * the concept of System.getProperty(). This class adds the
 * following features:
 *
 * <ul>
 * <li> A hierarchical property definition structure.
 * <li> Typed property retrieval with default values.
 * <li> Hierarchical overrides allowings properties to
 * be overridden on a per user or per host or per host
 * and user basis.
 * <li> The ability to load from any valid resource, including
 * files stored in JAR files, files located via the file system,
 * which includes networked file systems, as well as any other
 * resource that can be identified via a URL, including web pages,
 * FTP-ed files, and more.
 * </ul>
 *
 * <p>
 * Here is how it works. We have <em>six</em> levels of
 * properties which are loaded based on various settings.
 * The levels look like this:
 *
 * <ol>
 * <li> Hardwired defaults.
 * <li> Application defined defaults.
 * <li> Defaults resource.
 * <li> System level resource list.
 * <li> Application property file.
 * <li> Application resource list.
 * </ol>
 *
 * <p>
 * Resource lists are colon (:) separated strings listing
 * resource names to be loaded into the properties.
 *
 * <p>
 * In a typical deployment, the developer will place the
 * defaults resource in the JAR file containing the application's
 * classes. This file will then define the application properties
 * file, which will usually be left empty allowing the user to
 * place all customizations in this local file. For distributed
 * applications, system resources will typically be supplied via
 * simple web pages, which allows for automatic updates of many
 * properties. The application resource list is then typically
 * reserved for specific user customizations, or for distributed
 * customizations, or updates.
 *
 * <p>
 * Typically, the System Resource List is defined in the
 * Defaults Resource. However, it can also be defined by
 * the application defaults, or can be hardwired into the
 * code if desired. Further, the Application Resource List
 * is typically defined by the Application Property File,
 * although it can be defined in any of the previous loaded
 * resources.
 *
 * <p>
 * Also note that the application prefix can be set at any
 * point up to and including the defaults resource. After
 * the defaults resource is loaded, the prefix property is
 * consulted, and if set, is used as the new application
 * prefix. The prefix property is named by adding together
 * the application's package name and the string 'propertyPrefix'.
 * Thus, the prefix property for 'com.ice.jcvs' would be named
 * 'com.ice.jcvs.propertyPrefix', and would typically be set
 * in the defaults resource.
 *
 * <p>
 * Things the application <strong>must</strong> do to use this class:
 *
 * <ul>
 * <li> Set the property prefix via UserProperties.setPropertyPrefix()
 * <li> Set the defaults resource via UserProperties.setDefaultsResource()
 * <li> Process any arguments via UserProperties.processOptions()
 * <li> Load all properties.
 * </ul>
 *
 * <p>
 * Here is an example from a typical main():
 * <pre>
 *		UserProperties.setPropertyPrefix( "WebTool." );
 *
 *		UserProperties.setDefaultsResource
 *			( "/com/ice/webtool/defaults.txt" );
 *
 *		// PROCESS PROPERTIES OPTIONS
 *		// The returned args are those not processed.
 *		args = UserProperties.processOptions( args );
 *
 *		// Now app should process remaining arguments...
 *
 *		// LOAD PROPERTIES
 *		UserProperties.loadProperties( "com.ice.webtool", null );
 * </pre>
 *
 * <p>
 * Properties are accessed via the getProperty() methods, which
 * provide versions for String, int, double, and boolean values.
 * Any property is looked for as follows:
 *
 * <ol>
 * <li> fullname.osname.username
 * <li> fullname.username
 * <li> fullname.osname
 * <li> fullname
 * </ol>
 *
 * Whichever property is found first is returned.
 * The <em>fullname</em> consists of the property name prefixed
 * by the application's property prefix. The username and osname
 * suffixes are used to override general properties by os or by
 * user. These suffixes are printed at startup to System.err.
 * The osname suffix is the osname with spaces replaced by
 * underscores. The username suffix is the user's name with
 * spaces replaced by underscores.
 *
 * <p>
 * If the property name starts with a period
 * (.), then the prefix is not added to get the full name.
 * If the property name ends with a period (.), then none
 * of the suffixes are applied, and only the name is used
 * to search for a property.
 *
 * <p>
 * Thus, while the property name "mainWindow.x" would match
 * a property definition named "prefix.mainWindow.x.user", the
 * property ".mainWindow.x." would match a property with only
 * that name and no prefix or suffix, and the property
 * "mainWindow.x." would match "prefix.mainWindow.x" only
 * and not allow for suffix overrides.
 *
 * <p>
 * The following parameters are understood by processOptions():
 *
 * <ul>
 * <li> -propPrefix prefix     -- sets the property prefix.
 * <li> -propFile filename     -- sets the application property file.
 * <li> -propDefaults resource -- sets the defaults resource name.
 * <li> -propDebug             -- turns on debugging of property handling.
 * <li> -propVerbose           -- turns on verbosity.
 * <li> -propOS osname         -- set the property os suffix.
 * <li> -propUser username     -- set the property user name suffix.
 * </ul>
 *
 * @version $Revision: 1.10 $
 * @author Tim Endres,
 *    <a href="mailto:time@ice.com">time@ice.com</a>.
 */

public abstract class
UserProperties
	{
	private static final String		PREFIX_PROPERTY = "propertyPrefix";
	private static final String		DEFAULTS_RSRC_NAME = ".com.ice.global.defaultsResource.";

	private static final String		GLOBAL_RSRCLIST_NAME = ".com.ice.global.propertyResourceList";
	private static final String		GLOBAL_RSRC_PREFIX = ".com.ice.global.propertyResource.";

	private static final String		APP_RSRCLIST_NAME = ".com.ice.local.propertyResourceList";
	private static final String		APP_RSRC_PREFIX = ".com.ice.local.propertyResource.";

	private static final String		LOCAL_PROPERTY = "global.localPropertyFile";
	private static final String		LOCAL_DEFAULT = null;

	private static final String		DYNAMIC_PROPERTY_VERSION = "1.0";


	private static boolean		debug;
	private static boolean		verbose;

	private static String		osname;
	private static String		userName;
	private static String		userHome;
	private static String		javaHome;

	private static String		prefix;

	private static String		osSuffix;
	private static String		userSuffix;

	private static String		defaultsResource;
	private static String		localPropertyFile;

	/**
	 * This is a Hashtable of Vectors. The table keys are
	 * dynamic property package names. Each Vector contains
	 * the list of property names in the dynamic package.
	 */
	private static Hashtable	dynKeysTable;

	/**
	 * This is a Hashtable of Strings. The table keys are
	 * dynamic property package names. Each String is the
	 * pathname to the property file for the dynamic package.
	 */
	private static Hashtable	dynPathTable;

	/**
	 * Used for temporary working properties.
	 */
	private static Properties	workingProps;


	static
		{
		UserProperties.debug = false;
		UserProperties.verbose = false;

		UserProperties.prefix = null;

		UserProperties.defaultsResource = null;
		UserProperties.localPropertyFile = null;

		UserProperties.dynKeysTable = new Hashtable();
		UserProperties.dynPathTable = new Hashtable();
		UserProperties.workingProps = new Properties();

		UserProperties.osname = System.getProperty( "os.name" );
		UserProperties.userName = System.getProperty( "user.name" );
		UserProperties.userHome = System.getProperty( "user.home" );
		UserProperties.javaHome = System.getProperty( "java.home" );

		UserProperties.osSuffix =
			UserProperties.osname.replace( ' ', '_' );
		UserProperties.userSuffix =
			UserProperties.userName.replace( ' ', '_' );
		}

	static public String
	getOSName()
		{
		return UserProperties.osname;
		}

	static public String
	getUserHome()
		{
		return UserProperties.userHome;
		}

	static public String
	getUserName()
		{
		return UserProperties.userName;
		}

	static public void
	setDebug( final boolean debug )
		{
		UserProperties.debug = debug;
		}

	static public void
	setVerbose( final boolean verbose )
		{
		UserProperties.verbose = verbose;
		}

	static public void
	setLocalPropertyFile( final String fileName )
		{
		UserProperties.localPropertyFile = fileName;
		}

	static public void
	setDefaultsResource( final String rsrcName )
		{
		UserProperties.defaultsResource = rsrcName;
		}

	static public void
	setOSSuffix( final String suffix )
		{
		UserProperties.osSuffix = suffix;
		}

	static public void
	setUserSuffix( final String suffix )
		{
		UserProperties.userSuffix = suffix;
		}

	static public void
	setPropertyPrefix( final String prefix )
		{
		if ( prefix.endsWith( "." ) )
			UserProperties.prefix = prefix;
		else
			UserProperties.prefix = prefix + ".";
		}

	static public String
	getPropertyPrefix()
		{
		return UserProperties.prefix;
		}

	static public String
	getLineSeparator()
		{
		return System.getProperty( "line.separator", "\n" );
		}

	static public Font
	getFont( final String name, final Font defaultFont )
		{
		return
			Font.getFont
				( UserProperties.prefixedPropertyName( name ),
					defaultFont );
		}

	static public Color
	getColor( final String name, final Color defaultColor )
		{
		return
			Color.getColor
				( UserProperties.prefixedPropertyName( name ),
					defaultColor );
		}

	static public String
	prefixedPropertyName( final String name )
		{
		return UserProperties.prefix + name;
		}

	public static String
	normalizePropertyName( final String name )
		{
		if ( name.startsWith( "." ) )
			return name.substring(1);
		else
			return UserProperties.prefixedPropertyName( name );
		}

	/**
	 * Retrieve a system string property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default string value.
	 * @return The string value of the named property.
	 */

	static private String
	getOverridableProperty( final String name, final String defval )
		{
		String		value = null;
		String		overName = null;
		String		fullName = null;

		fullName = UserProperties.normalizePropertyName( name );

		if ( fullName.endsWith( "." ) )
			{
			fullName = fullName.substring( 0, fullName.length() - 1 );
			value = System.getProperty( fullName, defval );
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ fullName + " = '" + value + "'" );
			return value;
			}

		if ( UserProperties.osSuffix != null
				&& UserProperties.userSuffix != null )
			{
			overName =
				fullName + "." + UserProperties.osSuffix
				 + "." + UserProperties.userSuffix;
			value = System.getProperty( overName, null );
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ overName + " = '" + value + "'" );
			if ( value != null )
				return value;
			}

		if ( UserProperties.userSuffix != null )
			{
			overName = fullName + "." + UserProperties.userSuffix;
			value = System.getProperty( overName, null );
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ overName + " = '" + value + "'" );
			if ( value != null )
				return value;
			}

		if ( UserProperties.osSuffix != null )
			{
			overName = fullName + "." + UserProperties.osSuffix;
			value = System.getProperty( overName, null );
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ overName + " = '" + value + "'" );
			if ( value != null )
				return value;
			}

		if ( value == null )
			{
			value = System.getProperty( fullName, null );
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ fullName + " = '" + value + "'" );
			}

		if ( value == null )
			{
			value = defval;
			if ( UserProperties.debug )
				System.err.println
					( "UserProperties.getOverridableProperty: "
						+ name + " defaulted to '" + value + "'" );
			}

		return value;
		}

	/**
	 * Retrieve a system string property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default string value.
	 * @return The string value of the named property.
	 */

	static public String
	getProperty( final String name, final String defval )
		{
		final String result =
			UserProperties.getOverridableProperty
				( name, defval );
		return result;
		}

	/**
	 * Retrieve a system integer property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default integer value.
	 * @return The integer value of the named property.
	 */

	static public int
	getProperty( final String name, final int defval )
		{
		int result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			try { result = Integer.parseInt( val ); }
				catch ( final NumberFormatException ex )
					{ result = defval; }
			}

		return result;
		}

	/**
	 * Retrieve a system long property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default integer value.
	 * @return The integer value of the named property.
	 */

	static public long
	getProperty( final String name, final long defval )
		{
		long result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			try { result = Long.parseLong( val ); }
				catch ( final NumberFormatException ex )
					{ result = defval; }
			}

		return result;
		}

	/**
	 * Retrieve a system double property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default double value.
	 * @return The double value of the named property.
	 */

	static public double
	getProperty( final String name, final double defval )
		{
		double result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			try { result = Double.valueOf( val ).doubleValue(); }
				catch ( final NumberFormatException ex )
					{ result = defval; }
			}

		return result;
		}

	/**
	 * Retrieve a system boolean property.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default boolean value.
	 * @return The boolean value of the named property.
	 */

	static public boolean
	getProperty( final String name, final boolean defval )
		{
		boolean result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			if ( val.equalsIgnoreCase( "T" )
					|| val.equalsIgnoreCase( "TRUE" )
					|| val.equalsIgnoreCase( "Y" )
					|| val.equalsIgnoreCase( "YES" ) )
				result = true;
			else if ( val.equalsIgnoreCase( "F" )
					|| val.equalsIgnoreCase( "FALSE" )
					|| val.equalsIgnoreCase( "N" )
					|| val.equalsIgnoreCase( "NO" ) )
				result = false;
			}

		return result;
		}

	/**
	 * Retrieve a system string array property list. String
	 * arrays are represented by colon separated strings.
	 * Returns a provided default value if the property
	 * is not defined.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default boolean value.
	 * @return The string array value of the named property.
	 */

	static public String[]
	getStringArray( final String name, final String[] defval )
		{
		String[] result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			result = StringUtilities.splitString( val, ":" );
			}

		return result;
		}

	static public Vector
	getStringVector( final String name, final Vector defval )
		{
		Vector result = defval;

		final String[] sa =
			UserProperties.getStringArray
				( name, null );

		if ( sa != null )
			{
			result = new Vector();
			for ( int s = 0 ; s < sa.length ; ++s )
				result.addElement( sa[s] );
			}

		return result;
		}

	/**
	 * Retrieve a system Class constant property.
	 *
	 * @param name The name of the property to retrieve.
	 * @param defval A default integer value.
	 * @return The integer value of the named property.
	 */

	static public int
	getClassConstant( final String name, final int defval )
		{
		int result = defval;

		final String val = UserProperties.getProperty( name, null );

		if ( val != null )
			{
			final int index = val.lastIndexOf( "." );

			if ( index > 0 )
				{
				try {
					final String className = val.substring( 0, index );
					final String constName = val.substring( index + 1);
					final Class cls = Class.forName( className );
					final Field fld = cls.getField( constName );
					result = fld.getInt( null );
					}
				catch ( final Exception ex )
					{
					result = defval;
					ICETracer.traceWithStack
						( "Exception getting constant." );
					}
				}
			}

		return result;
		}

	/**
	 * Establishes critical default properties.
	 *
	 * @param props The system properties to add properties into.
	 */

	static public void
	defaultProperties( final Properties props )
		{
		props.put( "com.ice.util.UserProperties.revision", "$Revision: 1.10 $" );
		props.put( "copyright", "Copyright (c) by Tim Endres" );

		//
		// Define the following to create a global
		// enterprise-wide defaults resource...
		// e.g.
		//
		// props.put
		//	( UserProperties.DEFAULTS_RSRC_NAME,
		//		"http://www.ice.com/properties/defaults.txt" );
		//
		}

	static public void
	includeProperties( final Properties into, final Properties from )
		{
		final Enumeration enumeration = from.keys();

		for ( ; enumeration.hasMoreElements() ; )
			{
			Object key = null;

			try { key = enumeration.nextElement(); }
				catch ( final NoSuchElementException ex )
					{ key = null; }

			if ( key != null )
				{
				into.put( key, from.get( key ) );
				}
			}
		}

	static public void
	addDefaultProperties( final Properties props, final Properties defaultProps )
		{
		UserProperties.includeProperties( props, defaultProps );
		}

	/**
	 * Loads a properties stream into the System properties table.
	 *
	 * @param path The properties data's input stream.
	 * @param props The system properties to add properties into.
	 */

	static private boolean
	loadPropertiesStream( final InputStream in, final Properties props )
		throws IOException
		{
		props.load( in );
		return true;
		}

	static private void
	doLoadPropertiesFile( final String path, final Properties props, final Properties loaded )
		throws IOException
		{
		FileInputStream	in;
		final boolean			result = true;

		try { in = new FileInputStream( path ); }
		catch ( final IOException ex )
			{
			throw new IOException
				( "opening property file '" + path
					+ "' - " + ex.getMessage() );
			}

		try {
			if ( loaded != null )
				{
				UserProperties.loadPropertiesStream( in, loaded );
				UserProperties.includeProperties( props, loaded );
				}
			else
				{
				UserProperties.loadPropertiesStream( in, props );
				}
			}
		catch ( final IOException ex )
			{
			throw new IOException
				( "loading property file '" + path
					+ "' - " + ex.getMessage() );
			}

		try { in.close(); }
		catch ( final IOException ex )
			{
			throw new IOException
				( "closing property file '" + path
					+ "' - " + ex.getMessage() );
			}
		}

	/**
	 * Loads a named properties file into the System properties table.
	 *
	 * @param path The properties file's pathname.
	 * @param props The system properties to add properties into.
	 * @param loaded If not null, insert properties here before loading.
	 */

	static private boolean
	loadPropertiesFile( final String path, final Properties props, final Properties loaded )
		{
		final FileInputStream	in;
		boolean			result = true;

		if ( UserProperties.debug )
			System.err.println
				( "Loading property file '" + path + "'." );

		try {
			UserProperties.doLoadPropertiesFile( path, props, loaded );
			}
		catch ( final IOException ex )
			{
			System.err.println
				( "ERROR " + ex.getMessage() );
			result = false;
			}

		if ( result )
			System.err.println
				( "Loaded property file '" + path + "'." );

		return result;
		}

	/**
	 * Loads a named properties file into the System properties table.
	 * This method fails <em>silently,</em>, as we do not care if the
	 * file is there, and it is a feature that the user can remove the
	 * file to <em>reset</em> their settings.
	 *
	 * @param path The properties file's pathname.
	 * @param props The system properties to add properties into.
	 */

	static private void
	loadDynamicProperties( final String name, final String path )
		{
		final Properties	dynProps = new Properties();
		final Properties	sysProps = System.getProperties();

		if ( UserProperties.debug )
			System.err.println
				( "Loading  '" + name
					+ "' protperties from '" + path + "'." );

		try {
			UserProperties.doLoadPropertiesFile( path, sysProps, dynProps );
			UserProperties.addDynamicPropertyKeys( name, dynProps );
			System.err.println
				( "Loaded '" + name + "' properties from '" + path + "'." );
			}
		catch ( final IOException ex )
			{
			// Silently fail on dynamic property files!
			}
		}

	static private boolean
	loadPropertiesResource( final String name, final Properties props )
		{
		InputStream	in;
		boolean		result = false;

		if ( UserProperties.debug )
			System.err.println
				( "Load properties resource '" + name + "'" );

		try {
			in = ResourceUtilities.openNamedResource( name );
			UserProperties.loadPropertiesStream( in, props );
			in.close();
			result = true;
			}
		catch ( final java.io.IOException ex )
			{
			System.err.println
				( "ERROR loading properties resource '"
					+ name + "' - " + ex.getMessage() );
			}

		return result;
		}

	private static void
	loadPropertyResourceList(
			final String listPropName, final String rsrcPrefix, final Properties props )
		{
		final String rsrcListStr =
			UserProperties.getProperty( listPropName, null );

		if ( rsrcListStr != null )
			{
			final String[] rsrcList =
				StringUtilities.splitString( rsrcListStr, ":" );

			for ( int rIdx = 0
					; rsrcList != null && rIdx < rsrcList.length
						; ++rIdx )
				{
				final String rsrcTag = rsrcPrefix + rsrcList[rIdx];

				final String rsrcName =
					UserProperties.getProperty( rsrcTag, null );

				if ( rsrcName != null )
					{
					final boolean result =
						UserProperties.loadPropertiesResource
							( rsrcName, props );

					if ( ! result )
						{
						System.err.println
							( "ERROR loading property resource '"
								+ rsrcName + "'" );
						}
					}
				}
			}
		}

	// UNDONE
	// This routine need to use JNDI (?) to get a 'global' property
	// file name (typically on a network mounted volume) to read,
	// which should in turn set the name of the local property file.
	// JNDI should also set some 'critical' properties, such as
	// the important OTA hostnames, service ports, etc.

	// REVIEW
	// UNDONE
	// This routine should have a 'filter' that filters out all
	// global properties that do not start with prefix?

	/**
	 * Load all related properties for this application.
	 * This class method will look for a global properties
	 * file, loading it if found, then looks for a local
	 * properties file and loads that.
	 */

	static public void
	loadProperties( final String packageName, final Properties appProps )
		{
		boolean			result;
		File			propFile;
		String			propPath;
		final String			propName;
		String			rsrcName;

		if ( UserProperties.debug )
			{
			UserProperties.printContext( System.err );
			}

		final Properties sysProps =
			System.getProperties();

		if ( sysProps == null )
			return;

		UserProperties.defaultProperties( sysProps );

		//
		// ---- PROCESS THE DEFAULT PROPERTIES RESOURCE
		//
		rsrcName = UserProperties.defaultsResource;
		if ( rsrcName == null )
			{
			rsrcName =
				UserProperties.getProperty
					( UserProperties.DEFAULTS_RSRC_NAME, null );
			}

		if ( UserProperties.debug )
			System.err.println
				( "Default Properties Resource '" + rsrcName + "'" );

		if ( rsrcName != null )
			{
			result =
				UserProperties.loadPropertiesResource
					( rsrcName, sysProps );

			System.err.println
				( "Loaded "
					+ ( result ? "the " : "no " )
					+ "default properties." );
			}

		//
		// ---- PROCESS THE APPLICATION DEFAULT PROPERTIES
		//
		if ( appProps != null )
			{
			if ( UserProperties.debug )
				System.err.println
					( "Adding application default properties." );

			UserProperties.addDefaultProperties
				( sysProps, appProps );
			}

		//
		// ---- PROCESS THE PREFIX PROPERTY
		//
		final String newPrefix = UserProperties.prefix;
		if ( UserProperties.debug )
			System.err.println
				( "Prefix '" + newPrefix + "'" );

		if ( newPrefix == null )
			{
			UserProperties.getProperty
				( packageName + "."
					+ UserProperties.PREFIX_PROPERTY, null );

			if ( newPrefix != null )
				{
				if ( UserProperties.debug )
					System.err.println
						( "Prefix via property '" + newPrefix + "'" );

				UserProperties.setPropertyPrefix( newPrefix );
				if ( UserProperties.verbose )
					System.err.println
						( "Property prefix set to '" + newPrefix + "'" );
				}
			}

		//
		// ---- PROCESS THE GLOBAL PROPERTIES RESOURCES
		//
		UserProperties.loadPropertyResourceList
			( UserProperties.GLOBAL_RSRCLIST_NAME,
				UserProperties.GLOBAL_RSRC_PREFIX, sysProps );

		//
		// ---- PROCESS THE GLOBAL DYNAMIC PROPERTY REGISTRATIONS
		//
		UserProperties.processDynamicProperties();

		//
		// ---- PROCESS THE LOCAL PROPERTIES FILE
		//
		propPath = UserProperties.localPropertyFile;
		if ( propPath == null )
			{
			propPath =
				UserProperties.getProperty
					( UserProperties.LOCAL_PROPERTY,
						UserProperties.LOCAL_DEFAULT );
			}

		if ( UserProperties.debug )
			System.err.println
				( "Local property file '" + propPath + "'" );

		if ( propPath != null )
			{
			propFile = new File( propPath );
			if ( propFile.exists() )
				{
				result =
					UserProperties.loadPropertiesFile
						( propPath, sysProps, null );

				if ( ! result )
					{
					System.err.println
						( "ERROR loading local property file '"
							+ propPath + "'" );
					}
				}
			}

		//
		// ---- PROCESS THE GLOBAL PROPERTIES RESOURCES
		//
		UserProperties.loadPropertyResourceList
			( UserProperties.APP_RSRCLIST_NAME,
				UserProperties.APP_RSRC_PREFIX, sysProps );
		}

	private static void
	processDynamicProperties()
		{
		//
		// First, register any dynamic property definitions
		// defined by global properties.
		//
		final Properties sysProps = System.getProperties();

		final String dynPropList =
			sysProps.getProperty( "global.dynamicPropList", null );

		if ( dynPropList != null )
			{
			final String[] dynList =
				StringUtilities.splitString( dynPropList, ":" );

			for ( int sIdx = 0 ; sIdx < dynList.length ; ++sIdx )
				{
				final String dynName = dynList[sIdx];

				final String pathPropName =
					"global.dynamicPropFile." + dynName;

				String dynPath =
					sysProps.getProperty( pathPropName, null );

				if ( dynPath != null )
					{
					if ( dynPath.startsWith( "~" + File.separator ) )
						{
						dynPath =
							sysProps.getProperty( "user.home", "" )
								+ dynPath.substring( 2 );
						}

					UserProperties.registerDynamicProperties
						( dynName, dynPath, new Properties() );
					}
				}
			}

		// Now, we do the actual loading of dynamic properties.
		final Enumeration names = UserProperties.dynKeysTable.keys();
		for ( ; names.hasMoreElements() ; )
			{
			final String name = (String) names.nextElement();

			final String path = (String)
				UserProperties.dynPathTable.get( name );

			UserProperties.loadDynamicProperties( name, path );
			}
		}

	public static void
	registerDynamicProperties( final String name, final String path, final Properties props )
		{
		UserProperties.dynPathTable.put( name, path );
		UserProperties.addDynamicPropertyKeys( name, props );
		}

	private static void
	addDynamicPropertyKeys( final String name, final Properties dynProps )
		{
		Vector dynKeys = (Vector)
			UserProperties.dynKeysTable.get( name );

		if ( dynKeys == null )
			{
			dynKeys =
				dynProps == null
					? new Vector( 0 )
					: new Vector( dynProps.size() );

			UserProperties.dynKeysTable.put( name, dynKeys );
			}

		if ( dynProps != null )
			{
			// Ensure all key names are
			final Enumeration keys = dynProps.keys();
			for ( ; keys.hasMoreElements() ; )
				{
				final String keyName = (String)keys.nextElement();
				if ( ! dynKeys.contains( keyName ) )
					dynKeys.addElement( keyName );
				}
			}
		}

	/**
	 * This method expects the property keys to be
	 * <strong>normalized</strong>, meaning that they are
	 * the full property name with the prefix added on.
	 */

	private static void
	copyDynamicProperties( final String name, final Properties props )
		{
		final String path = (String) UserProperties.dynPathTable.get( name );
		final Vector keys = (Vector) UserProperties.dynKeysTable.get( name );

		if ( keys == null || path == null )
			throw new NoSuchElementException
				( "you have not registered the dynamic property "
					+ "package named '" + name + "'" );

		final Properties sysProps = System.getProperties();

		try {
			final Enumeration enumeration = props.keys();
			for ( ; enumeration.hasMoreElements() ; )
				{
				final String key = (String)enumeration.nextElement();
				if ( key != null )
					{
					final String normalKey =
						UserProperties.normalizePropertyName( key );

					sysProps.put( normalKey, props.get( key ) );

					if ( ! keys.contains( normalKey ) )
						keys.addElement( normalKey );
					}
				}
			}
		catch ( final NoSuchElementException ex )
			{ }
		}

	public static void
	setDynamicProperties( final String name, final Properties props )
		{
		UserProperties.copyDynamicProperties( name, props );
		}

	/**
	 * This method expects the property keys to be <strong>not</strong>
	 * <em>normalized</em>, meaning that they are the full
	 * property name with the prefix added on.
	 */

	public static void
	setDynamicProperty( final String name, final String propName, final String propValue )
		{
		UserProperties.workingProps.clear();
		UserProperties.workingProps.put( propName, propValue );
		UserProperties.setDynamicProperties
			( name, UserProperties.workingProps );
		}

	/**
	 * This method removes a property from the dynamic properties.
	 */

	public static void
	removeDynamicProperty( final String name, final String propName )
		{
		final String path = (String) UserProperties.dynPathTable.get( name );
		final Vector keys = (Vector) UserProperties.dynKeysTable.get( name );

		if ( keys == null || path == null )
			throw new NoSuchElementException
				( "you have not registered the dynamic property "
					+ "package named '" + name + "'" );

		final String normalKey =
			UserProperties.normalizePropertyName( propName );

		if ( keys.contains( normalKey ) )
			{
			keys.removeElement( normalKey );
			System.getProperties().remove( normalKey );
			}
		}

	public static void
	saveDynamicProperties( final String name )
		throws IOException
		{
		final String path = (String) UserProperties.dynPathTable.get( name );
		final Vector keys = (Vector) UserProperties.dynKeysTable.get( name );

		if ( keys == null || path == null )
			throw new NoSuchElementException
				( "you have not registered the dynamic property "
					+ "package named '" + name + "'" );

		final Properties dynProps = new Properties();
		final Properties sysProps = System.getProperties();

		final int count = keys.size();
		for ( int idx = 0 ; idx < count ; ++idx )
			{
			final String pName = (String) keys.elementAt(idx);
			dynProps.put( pName, sysProps.get( pName ) );
			}

		UserProperties.saveDynamicPropFile( name, path, dynProps );
		}

	//
	// UNDONE
	// We should use an intermediate file and move at completion. This
	// would eliminate the file being trashed on an IOException.
	//
	private static void
	saveDynamicPropFile( final String name, final String path, final Properties dynProps )
		throws IOException
		{
		final String eol = System.getProperty( "line.separator", "\n" );
		final String comment = eol +
			"## --------------------  W A R N I N G  -------------------- " + eol +
			"#  This file is automatically generated." + eol +
			"#  Any changes you make to this file will be overwritten." + eol +
			"## ---------------------------------------------------------" + eol +
			"#";

		final FileOutputStream out =
			new FileOutputStream( path );

		dynProps.put(
			"global.dynPropVersion." + name,
			UserProperties.DYNAMIC_PROPERTY_VERSION );

		dynProps.save( out, comment );

		out.close();
		}

	public static void
	printContext( final PrintStream out )
		{
		out.println
			( "os.name    = '" + UserProperties.osname + "'" );
		out.println
			( "user.name  = '" + UserProperties.userName + "'" );
		out.println
			( "user.home  = '" + UserProperties.userHome + "'" );
		out.println
			( "java.home  = '" + UserProperties.javaHome + "'" );

		out.println( "" );

		out.println
			( "prefix     = '" + UserProperties.prefix + "'" );
		out.println
			( "osSuffix   = '" + UserProperties.osSuffix + "'" );
		out.println
			( "userSuffix = '" + UserProperties.userSuffix + "'" );

		out.println( "" );
		}

	public static void
	printUsage( final PrintStream out )
		{
		out.println
			( "Properties options:" );

		out.println
			( "   -propDebug             -- "
				+ "turns on debugging of property loading" );
		out.println
			( "   -propVerbose           -- "
				+ "turns on verbose messages during loading" );

		out.println
			( "   -propDefaults rsrcName -- "
				+ "sets default properties resource name" );
		out.println
			( "   -propFile path         -- "
				+ "sets application property file path" );

		out.println
			( "   -propOS suffix         -- "
				+ "sets the os suffix" );
		out.println
			( "   -propUser suffix       -- "
				+ "sets the user suffix" );
		out.println
			( "   -propPrefix prefix     -- "
				+ "sets application property prefix" );
		}

	static public String []
	processOptions( final String [] args )
		{
		final Vector newArgs = new Vector( args.length );

		for ( int iArg = 0 ; iArg < args.length ; ++iArg )
			{
			if ( args[iArg].equals( "-propPrefix" )
						&& iArg + 1 < args.length )
				{
				UserProperties.setPropertyPrefix( args[++iArg] );
				}
			else if ( args[iArg].equals( "-propFile" )
						&& iArg + 1 < args.length )
				{
				UserProperties.setLocalPropertyFile( args[++iArg] );
				}
			else if ( args[iArg].equals( "-propDefaults" )
						&& iArg + 1 < args.length )
				{
				UserProperties.setDefaultsResource( args[++iArg] );
				}
			else if ( args[iArg].equals( "-propDebug" ) )
				{
				UserProperties.setDebug( true );
				}
			else if ( args[iArg].equals( "-propVerbose" ) )
				{
				UserProperties.setVerbose( true );
				}
			else if ( args[iArg].equals( "-propOS" )
						&& iArg + 1 < args.length )
				{
				UserProperties.setOSSuffix( args[++iArg] );
				}
			else if ( args[iArg].equals( "-propUser" )
						&& iArg + 1 < args.length )
				{
				UserProperties.setUserSuffix( args[++iArg] );
				}
			else
				{
				newArgs.addElement( args[iArg] );
				}
			}

		final String[] result = new String[ newArgs.size() ];
		for ( int i = 0 ; i < newArgs.size() ; ++i )
			result[i] = (String) newArgs.elementAt(i);

		return result;
		}

	}


