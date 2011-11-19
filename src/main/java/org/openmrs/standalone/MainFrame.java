/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.standalone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.openmrs.standalone.ApplicationController.DatabaseMode;

/**
 * Provides non command line (GUI) interface to the standalone launcher..
 */
public class MainFrame extends javax.swing.JFrame implements ActionListener, UserInterface {
	
	private ApplicationController appController;
	
	private int tomcatPort = UserInterface.DEFAULT_TOMCAT_PORT;
	
	private TextAreaWriter textAreaWriter;
	
	/** Creates new form ServerUI */
	public MainFrame(ApplicationController appController, String tomcatPort, String mysqlPort) {
		this.appController = appController;
		
		if(tomcatPort != null && tomcatPort.trim().length() > 0)
			this.tomcatPort = StandaloneUtil.fromStringToInt(tomcatPort);
		
		initComponents(mysqlPort);
	}
	
	private void initComponents(String mysqlPort) {
		
		try {
			// Set native look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		setSize(600, 300);
		setResizable(true);
		setLocationRelativeTo(null);
		
		menuBar = new JMenuBar();
		fileMenu = new JMenu();
		browserMenuItem = new JMenuItem();
		fileMenuSep = new JSeparator();
		quitMenuItem = new JMenuItem();
		
		setTitle(TITLE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		
		setStatus(UserInterface.STATUS_MESSAGE_STARTING);
		
		int preferredMetaKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		fileMenu.setText("File");
		browserMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, preferredMetaKey));
		browserMenuItem.setMnemonic('K');
		browserMenuItem.setText("Launch Browser");
		browserMenuItem.setEnabled(false);
		browserMenuItem.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browserMenuItemActionPerformed(evt);
			}
		});
		
		fileMenu.add(browserMenuItem);
		
		fileMenu.add(fileMenuSep);
		
		JMenuItem clearMenuItem = new JMenuItem();
		clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, preferredMetaKey));
		clearMenuItem.setMnemonic('C');
		clearMenuItem.setText("Clear Output");
		clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearMenuItemActionPerformed(evt);
			}
		});
		
		fileMenu.add(clearMenuItem);
		
		fileMenu.add(new JSeparator());
		
		quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, preferredMetaKey));
		quitMenuItem.setMnemonic('K');
		quitMenuItem.setText("Quit");
		quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				quitMenuItemActionPerformed(evt);
			}
		});
		
		fileMenu.add(quitMenuItem);
		
		menuBar.add(fileMenu);
		
		setJMenuBar(menuBar);
		
		mainPanel = new JPanel();
		portPanel = new JPanel();
		
		mainPanel.setLayout(new GridBagLayout());
		
		lblTomcatPort = new JLabel("Tomcat Port");
		txtTomcatPort = new JTextField(tomcatPort+"", 5);
		lblMySqlPort = new JLabel("MySQL Port");
		txtMySqlPort = new JTextField(mysqlPort, 5);
		txtLog = new JTextArea();
		
		btnStart = new JButton("Start");
		btnStop = new JButton("Stop");
		
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
		
		btnStart.addActionListener(this);
		btnStop.addActionListener(this);
		
		portPanel.add(lblTomcatPort);
		portPanel.add(txtTomcatPort);
		portPanel.add(lblMySqlPort);
		portPanel.add(txtMySqlPort);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0; //first row
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTH;
		
		mainPanel.add(portPanel, c);
		
		JPanel btnPanel = new JPanel();
		btnPanel.add(btnStart);
		btnPanel.add(btnStop);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1; //second row
		c.weightx = 1.0;
		mainPanel.add(btnPanel, c);
		
		txtLog.setLineWrap(true);
		txtLog.setWrapStyleWord(true);
		txtLog.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JScrollPane scrolltxt = new JScrollPane(txtLog);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2; //third row
		c.weightx = 1.0;
		c.weighty = 1.0; //request any extra vertical space
		mainPanel.add(scrolltxt, c);
		
		add(mainPanel);
		
		try {
			setIconImage(ImageIO.read(getClass().getResource("openmrs_logo_white.gif")));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//Redirect output and error streams to a text field.
		textAreaWriter = new TextAreaWriter(txtLog);
		PrintStream stream = new PrintStream(textAreaWriter);
		System.setOut(stream);
		System.setErr(stream);
		
		setAvailablePorts();
		//StandaloneUtil.setPortsAndMySqlPassword(txtMySqlPort.getText());
	}
	
	private void browserMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		appController.launchBrowser(tomcatPort);
	}
	
	private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		exitForm(null);
	}
	
	private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		textAreaWriter.clear();
	}
	
	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {
		try {
			//Prompt only if server is running.
			if (btnStop.isEnabled()) {
				if (JOptionPane.showConfirmDialog(this, UserInterface.PROMPT_EXIT, getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
					return;
			}
			
			setStatus(UserInterface.STATUS_MESSAGE_SHUTTINGDOWN);
			
			menuBar.setEnabled(false);
			btnStop.setEnabled(false);
			btnStart.setEnabled(false);
			
			appController.exit();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setStatus(String status) {
		setTitle(TITLE + " - [" + status + "]");
	}
	
	public void actionPerformed(ActionEvent event) {
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			if (event.getSource() == btnStart) {
				if (!validPorts())
					return;
				
				tomcatPort = StandaloneUtil.fromStringToInt(txtTomcatPort.getText());
				
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				
				setStatus(UserInterface.STATUS_MESSAGE_STARTING);
				
				appController.start();
			} else if (event.getSource() == btnStop) {
				
				if (JOptionPane.showConfirmDialog(this, UserInterface.PROMPT_STOPSERVER, getTitle(),
				    JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
					return;
				
				setStatus(UserInterface.STATUS_MESSAGE_STOPPING);
				btnStop.setEnabled(false);
				
				appController.stop();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public int getTomcatPort() {
		return tomcatPort;
	}
	
	public String getMySqlPort(){
		return txtMySqlPort.getText();
	}
	
	public void enableStop(boolean enable) {
		btnStop.setEnabled(enable);
		browserMenuItem.setEnabled(enable);
	}
	
	public void enableStart(boolean enable) {
		btnStart.setEnabled(enable);
	}
	
	/**
	 * Sets the default tomcat and mysql port numbers to those that are available.
	 */
	private void setAvailablePorts() {
		tomcatPort = StandaloneUtil.fromStringToInt(txtTomcatPort.getText());
		while (tomcatPort < StandaloneUtil.MAX_PORT_NUMBER) {
			if (!StandaloneUtil.isPortAvailable(tomcatPort))
				tomcatPort++;
			else
				break;
		}
		txtTomcatPort.setText(String.valueOf(tomcatPort));
		
		int mysqlPort = StandaloneUtil.fromStringToInt(txtMySqlPort.getText());
		while (mysqlPort < StandaloneUtil.MAX_PORT_NUMBER) {
			if (!StandaloneUtil.isPortAvailable(mysqlPort) || mysqlPort == tomcatPort)
				mysqlPort++;
			else
				break;
		}
		txtMySqlPort.setText(String.valueOf(mysqlPort));
	}
	
	/**
	 * Checks if the user has set allowed port numbers for both tomcat and mysql, and if not so,
	 * displays an error message telling them to enter another one while at the same time setting
	 * focus to the invalid port.
	 * 
	 * @return true if both ports are allowed, else false.
	 */
	private boolean validPorts() {
		tomcatPort = StandaloneUtil.fromStringToInt(txtTomcatPort.getText());
		if (!StandaloneUtil.isPortAvailable(tomcatPort)) {
			JOptionPane.showMessageDialog(this, "The Tomcat port is not available. Please enter another one.", getTitle(),
			    JOptionPane.ERROR_MESSAGE);
			
			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
					txtTomcatPort.requestFocus();
				}
			});
			
			return false;
		}
		
		int mySqlPort = StandaloneUtil.fromStringToInt(txtMySqlPort.getText());
		if (!StandaloneUtil.isPortAvailable(mySqlPort) || mySqlPort == tomcatPort) {
			JOptionPane.showMessageDialog(this, "The MySQL port is not available. Please enter another one.", getTitle(),
			    JOptionPane.ERROR_MESSAGE);
			
			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
					txtMySqlPort.requestFocus();
				}
			});
			
			return false;
		}
		
		return true;
	}
	
	private JMenuItem browserMenuItem;
	
	private JMenu fileMenu;
	
	private JSeparator fileMenuSep;
	
	private JMenuBar menuBar;
	
	private JMenuItem quitMenuItem;
	
	private JPanel mainPanel;
	
	private JPanel portPanel;
	
	private JTextField txtTomcatPort;
	
	private JLabel lblTomcatPort;
	
	private JTextField txtMySqlPort;
	
	private JLabel lblMySqlPort;
	
	private JButton btnStart;
	
	private JButton btnStop;
	
	private JTextArea txtLog;

	/**
     * @see org.openmrs.standalone.UserInterface#showInitialConfig()
     */
    public void showInitialConfig() {
    	final JDialog configDialog = new JDialog(this, "Configure your OpenMRS Installation", true);
    	JPanel content = new JPanel();
    	content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    	content.setLayout(new BorderLayout(10, 10)); //10,10 = hgap, vgap
    	configDialog.getContentPane().add(content);
    	
    	Font font = new Font(Font.SERIF, Font.PLAIN, 16);
    	
    	JLabel instructions = new JLabel("<html><b>Welcome to OpenMRS! OpenMRS can be configured in one of three ways, depending on<br/>your needs. Please click on the configuration that best meets your needs.</b><br/>(You will not see this next time you run OpenMRS)</html>");
    	instructions.setFont(font);
    	content.add(instructions, BorderLayout.NORTH);

    	final JButton useCurrent = new JButton("Do Not Modify the Database");

    	final JButton demoDatabase = new JButton("<html><h3>Demonstration mode</h3>Configures OpenMRS with a demonstration database. This is the quickest way to start up OpenMRS with some sample data to evaluate the system or experiment with features. (TO DO: implement this.)</html>");
    	demoDatabase.setEnabled(false);
    	colorHelper(demoDatabase, new Color(138, 191, 99));
    	
    	final JButton emptyDatabase = new JButton("<html><h3>Starter Implementation</h3>Configures OpenMRS with an empty database. (TO DO: replace this with the MVP/CIEL option.) If you are familiar with OpenMRS and want to start a new system, this is a good place to start.</html>");
    	colorHelper(emptyDatabase, new Color(255, 243, 136));
    	
    	final JButton expertMode = new JButton("<html><h3>Expert Mode</h3>Go through the initial setup wizard yourself.");
    	colorHelper(expertMode, new Color(255, 138, 138));

    	ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == emptyDatabase) {
					appController.setApplyDatabaseChange(DatabaseMode.EMPTY_DATABASE);
				} else if (e.getSource() == useCurrent) {
					appController.setApplyDatabaseChange(DatabaseMode.NO_CHANGES);
				} else if (e.getSource() == expertMode) {
					appController.setApplyDatabaseChange(DatabaseMode.USE_INITIALIZATION_WIZARD);
				}
				configDialog.dispose();
			}
		};

    	JPanel buttons = new JPanel();
    	buttons.setLayout(new GridLayout(0, 1, 10, 10)); // 0,1 -> vertical, 10,10=hgap,vgap
		for (JButton b : Arrays.asList(demoDatabase, emptyDatabase, expertMode)) {
			b.setFont(font);
			buttons.add(b);
			b.addActionListener(listener);
		}
		content.add(buttons, BorderLayout.CENTER);
    	
		configDialog.setPreferredSize(new Dimension(750, 600));
    	configDialog.pack();
		configDialog.setLocationRelativeTo(this);
    	configDialog.setVisible(true);
    }

	/**
     * Sets button color. (We can't just set background, because apparently on OSX this doesn't
     * work, since Apple wants things to match their default platform look and feel.
     * 
     * @param demoDatabase
     * @param color
     */
    private void colorHelper(JButton button, Color color) {
    	button.setBackground(new Color(138, 191, 99));
    	button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }
}
