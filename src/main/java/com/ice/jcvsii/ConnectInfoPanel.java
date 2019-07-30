/*
** Java CVS client application package.
** Copyright (c) 1997 by Timothy Gerard Endres
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


package com.ice.jcvsii;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.ice.cvsc.CVSRequest;
import com.ice.pref.UserPrefs;
import com.ice.util.AWTUtilities;


class		ConnectInfoPanel
extends		JPanel
implements	ItemListener, ActionListener
	{
	private JTextField		moduleText;
	private JTextField		hostNameText;
	private JTextField		repositoryText;
	private JTextField		argumentsText;
	private JTextField		exportDirText;

	private JRadioButton	rshRadio;
	private JRadioButton	sshRadio;
	private JRadioButton	inetdRadio;
	private JCheckBox		passwordCheck;

	private JLabel			userNameLbl;
	private JTextField		userNameText;
	private JLabel			passwordLbl;
	private JPasswordField	passwordText;


	ConnectInfoPanel( final String operation )
		{
		super();
		this.establishContents( operation );
		}

	public void
	loadPreferences( final String panName )
		{
		final UserPrefs prefs = Config.getPreferences();

		final String connMethod =
			prefs.getProperty
			(panName + '.' + ConfigConstants.INFOPAN_METHOD, "" );

		if ( connMethod != null )
			{
			this.setServerMode( connMethod.equals( "RSH" ) );
			this.setPServerMode( connMethod.equals( "INET" ) );
			this.setSecureServerMode( connMethod.equals( "SSH" ) );
			}

		this.setUserName
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_USER_NAME, "" ) );
		this.setServer
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_SERVER_NAME, "" ) );
		this.setModule
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_MODULE_NAME, "" ) );
		this.setRepository
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_REPOS_NAME, "" ) );
		this.setExportDirectory
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_EXPDIR_NAME, "" ) );
		this.setArguments
			( prefs.getProperty
				(panName + '.' + ConfigConstants.INFOPAN_ARGS_NAME, "" ) );
		}

	public void
	savePreferences( final String panName )
		{
		final UserPrefs prefs = Config.getPreferences();

		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_METHOD,
				  this.inetdRadio.isSelected() ? "INET"
				: this.sshRadio.isSelected() ? "SSH"
				: "RSH" );

		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_USER_NAME,
				this.getUserName() );
		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_SERVER_NAME,
				this.getServer() );
		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_MODULE_NAME,
				this.getModule() );
		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_REPOS_NAME,
				this.getRepository() );
		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_EXPDIR_NAME,
				this.getExportDirectory() );
		prefs.setProperty
			(panName + '.' + ConfigConstants.INFOPAN_ARGS_NAME,
				this.getArguments() );
		}

	private void
	setServerMode(final boolean state)
		{
		this.rshRadio.setSelected( state );
		this.sshRadio.setSelected( ! state );
		this.inetdRadio.setSelected( ! state );
		}

	private void
	setSecureServerMode(final boolean state)
		{
		this.rshRadio.setSelected( ! state );
		this.sshRadio.setSelected( state );
		this.inetdRadio.setSelected( ! state );
		}

	public void
	setPServerMode( final boolean state )
		{
		this.rshRadio.setSelected( ! state );
		this.sshRadio.setSelected( ! state );
		this.inetdRadio.setSelected( state );
		}

	public void
	setUsePassword( final boolean state )
		{
		this.passwordCheck.setSelected( state );
		}

	public int
	getConnectionMethod()
		{
		return
			this.inetdRadio.isSelected()
			? CVSRequest.METHOD_INETD
			: this.sshRadio.isSelected()
				? CVSRequest.METHOD_SSH
				: CVSRequest.METHOD_RSH;
		}

	public boolean
	isPServer()
		{
		return this.passwordCheck.isSelected()
					&& this.inetdRadio.isSelected();
		}

	public boolean
	isPasswordSelected()
		{
		return this.passwordCheck.isSelected();
		}

	public String
	getUserName()
		{
		return this.userNameText.getText();
		}

	private void
	setUserName(final String name)
		{
		this.userNameText.setText( name );
		}

	public String
	getPassword()
		{
		return new String( this.passwordText.getPassword() );
		}

	public String
	getModule()
		{
		return this.moduleText == null
					? "" : this.moduleText.getText();
		}

	private void
	setModule(final String name)
		{
		if ( this.moduleText != null )
			this.moduleText.setText( name );
		}

	public String
	getServer()
		{
		return this.hostNameText.getText();
		}

	private void
	setServer(final String name)
		{
		this.hostNameText.setText( name );
		}

	public String
	getRepository()
		{
		if ( repositoryText == null )
			return "";

		String repositorty = this.repositoryText.getText();

		if (!repositorty.isEmpty() && repositorty.charAt(repositorty.length() - 1) == '/')
			repositorty =
				repositorty.substring( 0, repositorty.length() - 1 );

		return repositorty;
		}

	private void
	setRepository(final String name)
		{
		if ( repositoryText != null )
			this.repositoryText.setText( name );
		}

	public String
	getArguments()
		{
		return this.argumentsText == null
					? "" : this.argumentsText.getText();
		}

	private void
	setArguments(final String args)
		{
		if ( argumentsText != null )
			this.argumentsText.setText( args );
		}

	public String
	getExportDirectory()
		{
		return this.exportDirText == null
					? "" : this.exportDirText.getText();
		}

	private void
	setExportDirectory(final String dir)
		{
		if ( exportDirText != null )
			this.exportDirText.setText( dir );
		}

	public String
	getImportDirectory()
		{
		return this.exportDirText == null
					? "" : this.exportDirText.getText();
		}

	public void
	setImportDirectory( final String dir )
		{
		if ( exportDirText != null )
			this.exportDirText.setText( dir );
		}

	public String
	getLocalDirectory()
		{
		return this.exportDirText == null
					? "" : this.exportDirText.getText();
		}

	public void
	setLocalDirectory( final String dir )
		{
		if ( exportDirText != null )
			this.exportDirText.setText( dir );
		}

	public void
	requestInitialFocus()
		{
		this.userNameText.requestFocus();
		}

	@Override
	public void
	actionPerformed( final ActionEvent evt )
		{
		if ( evt.getSource() == this.userNameText )
			{
			this.passwordText.requestFocus();
			}
		}

	@Override
	public void
	itemStateChanged( final ItemEvent event )
		{
		boolean relay = false;
		final Object item = event.getItemSelectable();

		if ( item == this.inetdRadio )
			{
			if ( this.inetdRadio.isSelected() )
				{
				this.passwordCheck.setEnabled( true );
				this.passwordCheck.setSelected( true );
				this.userNameLbl.setEnabled( true );
				this.userNameText.setEnabled( true );
				this.passwordText.setEnabled( true );
				this.userNameText.requestFocus();
				}

			relay = true;
			}
		else if ( item == this.rshRadio )
			{
			if ( this.rshRadio.isSelected() )
				{
				this.passwordCheck.setSelected( false );
				this.passwordCheck.setEnabled( false );
				this.passwordText.setEnabled( false );
				this.userNameLbl.setEnabled( true );
				this.userNameText.setEnabled( true );
				this.userNameText.requestFocus();
				}

			relay = true;
			}
		else if ( item == this.sshRadio )
			{
			if ( this.sshRadio.isSelected() )
				{
				this.passwordCheck.setSelected( true );
				this.passwordCheck.setEnabled( true );
				this.passwordText.setEnabled( true );
				this.userNameLbl.setEnabled( true );
				this.userNameText.setEnabled( true );
				this.userNameText.requestFocus();
				}

			relay = true;
			}
		else if ( item == this.passwordCheck )
			{
			if ( this.passwordCheck.isSelected() )
				{
				this.userNameLbl.setEnabled( true );
				this.userNameText.setEnabled( true );
				this.passwordText.setEnabled( true );
				this.userNameText.requestFocus();
				}
			else
				{
				this.userNameLbl.setEnabled( false );
				this.userNameText.setEnabled( false );
				this.passwordText.setEnabled( false );
				}

			relay = true;
			}

		if ( relay )
			{
			this.invalidate();
			this.validate();
			this.repaint();
			}
		}

	private void
	establishContents( final String operation )
		{
		JLabel lbl;
		int row = 0;

		this.setLayout( new GridBagLayout() );

		final ResourceMgr rmgr = ResourceMgr.getInstance();

		// ============== INPUT FIELDS PANEL ================

		final Container fldPan = new JPanel();
		fldPan.setLayout( new GridBagLayout() );

		// ------------------- Module -------------------
		if ( ! operation.equals( "test" )
				&& ! operation.equals( "initrep" ) )
			{
 			lbl = new MyLabel(rmgr.getUIString("name.for.cvsmodule"));
			AWTUtilities.constrain(
				fldPan, lbl,
				GridBagConstraints.NONE,
				GridBagConstraints.WEST,
				0, row, 1, 1, 0.0, 0.0 );

 			this.moduleText = new JTextField();
			AWTUtilities.constrain(
				fldPan, this.moduleText,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.CENTER,
				1, row++, 1, 1, 1.0, 0.0 );
			}

		// ------------------- Server -------------------
 		lbl = new MyLabel(rmgr.getUIString("name.for.cvsserver"));
		AWTUtilities.constrain(
			fldPan, lbl,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, row, 1, 1, 0.0, 0.0 );

 		this.hostNameText = new JTextField();
		AWTUtilities.constrain(
			fldPan, this.hostNameText,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.CENTER,
			1, row++, 1, 1, 1.0, 0.0 );

		// ------------------- Repository -------------------
 		lbl = new MyLabel(rmgr.getUIString("name.for.cvsrepos"));
		AWTUtilities.constrain(
			fldPan, lbl,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, row, 1, 1, 0.0, 0.0 );

 		this.repositoryText = new JTextField();
		AWTUtilities.constrain(
			fldPan, this.repositoryText,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.CENTER,
			1, row++, 1, 1, 1.0, 0.0 );

		// ------------------- Export/Import/Checkout Directory -------------------
		if ( operation.equals( "export" )
				|| operation.equals( "import" )
				|| operation.equals( "checkout" ) )
			{
				switch (operation) {
				case "export":
					lbl = new MyLabel(rmgr.getUIString("name.for.exportdir"));
					break;
				case "import":
					lbl = new MyLabel(rmgr.getUIString("name.for.importdir"));
					break;
				case "checkout":
					lbl = new MyLabel(rmgr.getUIString("name.for.checkoutdir"));
					break;
				}

			AWTUtilities.constrain(
				fldPan, lbl,
				GridBagConstraints.NONE,
				GridBagConstraints.WEST,
				0, row, 1, 1, 0.0, 0.0 );

 			this.exportDirText = new JTextField();
			AWTUtilities.constrain(
				fldPan, this.exportDirText,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.CENTER,
				1, row++, 1, 1, 1.0, 0.0 );
			}

		// ------------------- Arguments -------------------
		if ( operation.equals( "export" )
				|| operation.equals( "checkout" )
				|| operation.equals( "import" ) )
			{
 			lbl = new MyLabel(rmgr.getUIString("name.for.arguments"));
			AWTUtilities.constrain(
				fldPan, lbl,
				GridBagConstraints.NONE,
				GridBagConstraints.WEST,
				0, row, 1, 1, 0.0, 0.0 );

 			this.argumentsText = new JTextField();
			AWTUtilities.constrain(
				fldPan, this.argumentsText,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.CENTER,
				1, row++, 1, 1, 1.0, 0.0 );
			}


		// ============== SERVER DEFINES DIALOG BUTTON ================

		final AbstractButton defBtn =
			new JButton( rmgr.getUIString( "name.for.servers.button" ) );
		defBtn.addActionListener(evt ->
					{
					final ServersDialog dlg =
						new ServersDialog
							( (Frame) getTopLevelAncestor(),
								Config.getPreferences(),
									ConnectInfoPanel.this );
					dlg.show();

					final ServerDef def = dlg.getServerDefinition();

					if ( def != null )
						{
						if ( moduleText != null )
							moduleText.setText( def.getModule() );

						if ( hostNameText != null )
							hostNameText.setText( def.getHostName() );

						if ( userNameText != null )
							userNameText.setText( def.getUserName() );

						if ( repositoryText != null )
							repositoryText.setText( def.getRepository() );

						if ( def.getConnectMethod() == CVSRequest.METHOD_RSH )
							{
							rshRadio.setSelected( true );
							passwordCheck.setSelected( false );
							}
						else if ( def.getConnectMethod() == CVSRequest.METHOD_SSH )
							{
							sshRadio.setSelected( true );
							passwordCheck.setSelected( true );
							passwordText.requestFocus();
							}
						else
							{
							inetdRadio.setSelected( true );
							passwordCheck.setSelected( def.isPServer() );

							if ( def.isPServer() )
								passwordText.requestFocus();
							else if ( moduleText != null )
								moduleText.requestFocus();
							else
								repositoryText.requestFocus();
							}
						}
					}
			);

		// ============== USER LOGIN INFO PANEL ================

		final Container infoPan = new JPanel();
		infoPan.setLayout( new GridBagLayout() );

		final Container buttonPan = new JPanel();
		buttonPan.setLayout( new GridBagLayout() );
		AWTUtilities.constrain(
			infoPan, buttonPan,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, 0, 1, 1, 0.0, 0.0 );

		final Container inputPan = new JPanel();
		inputPan.setLayout( new GridBagLayout() );
		AWTUtilities.constrain(
			infoPan, inputPan,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.WEST,
			1, 0, 1, 1, 1.0, 0.0 );

		row = 0;

		// server method
		this.rshRadio =
			new JRadioButton
				( rmgr.getUIString( "name.for.connect.method.server" ) );
		this.rshRadio.addItemListener( this );
		AWTUtilities.constrain(
			buttonPan, this.rshRadio,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, row++, 1, 1, 0.0, 0.0 );

		// secure server method
		this.sshRadio =
			new JRadioButton
				( rmgr.getUIString( "name.for.connect.method.sserver" ) );
		this.sshRadio.addItemListener( this );
		AWTUtilities.constrain(
			buttonPan, this.sshRadio,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, row++, 1, 1, 0.0, 0.0 );

		// pserver method
		this.inetdRadio =
			new JRadioButton
				( rmgr.getUIString( "name.for.connect.method.pserver" ) );
		this.inetdRadio.addItemListener( this );
		AWTUtilities.constrain(
			buttonPan, this.inetdRadio,
			GridBagConstraints.NONE,
			GridBagConstraints.WEST,
			0, row, 1, 1, 0.0, 0.0 );

		row = 0;

		this.userNameLbl = new MyLabel(rmgr.getUIString("name.for.user.name"));
		this.userNameLbl.setForeground( Color.black );
		AWTUtilities.constrain(
			inputPan, this.userNameLbl,
			GridBagConstraints.NONE,
			GridBagConstraints.EAST,
			0, row, 1, 1, 0.0, 0.0 );

 		this.userNameText = new JTextField();
		this.userNameText.addActionListener( this );

		AWTUtilities.constrain(
			inputPan, this.userNameText,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.WEST,
			1, row++, 1, 1, 1.0, 0.0 );

		// Password Checkbox
		this.passwordCheck =
			new JCheckBox( rmgr.getUIString( "name.for.user.pass" ) );
		this.passwordCheck.addItemListener( this );
		AWTUtilities.constrain(
			inputPan, this.passwordCheck,
			GridBagConstraints.NONE,
			GridBagConstraints.EAST,
			0, row, 1, 1, 0.0, 0.0 );

		this.passwordText = new JPasswordField();
		this.passwordText.setEchoChar( '*' );
		AWTUtilities.constrain(
			inputPan, this.passwordText,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.WEST,
			1, row++, 1, 1, 1.0, 0.0 );


		final ButtonGroup btnGrp = new ButtonGroup();
		btnGrp.add( this.rshRadio );
		btnGrp.add( this.sshRadio );
		btnGrp.add( this.inetdRadio );

		row = 0;

		final Container topPan = new JPanel();
		topPan.setLayout( new GridBagLayout() );

		defBtn.setMargin( new Insets( 4, 8, 4, 8 ) ) ;
		AWTUtilities.constrain(
			topPan, defBtn,
			GridBagConstraints.NONE,
			GridBagConstraints.CENTER,
			0, row, 1, 1, 0.0, 0.0,
			new Insets( 4, 10, 4, 15 ) );

		AWTUtilities.constrain(
			topPan, new JSeparator( SwingConstants.VERTICAL ),
			GridBagConstraints.VERTICAL,
			GridBagConstraints.CENTER,
			1, row, 1, 1, 0.0, 1.0,
			new Insets( 0, 0, 0, 10 ) );

		AWTUtilities.constrain(
			topPan, infoPan,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.CENTER,
			2, row++, 1, 1, 0.7, 0.0 );

		row = 0;

		AWTUtilities.constrain(
			this, topPan,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.CENTER,
			0, row++, 1, 1, 1.0, 0.0 );

		final Component sep = new JSeparator(SwingConstants.HORIZONTAL );

		AWTUtilities.constrain(
			this, sep,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.CENTER,
			0, row++, 1, 1, 1.0, 0.0,
			new Insets( 3, 0, 5, 0 ) );

		AWTUtilities.constrain(
			this, fldPan,
			GridBagConstraints.HORIZONTAL,
			GridBagConstraints.WEST,
			0, row++, 1, 1, 1.0, 0.0 );
		}

	private static final
	class		MyLabel
	extends		JLabel
		{
		private MyLabel(final String text)
			{
			super( text );
			this.setBorder( new EmptyBorder( 0, 3, 0, 5 ) );
			}
		}

	}

