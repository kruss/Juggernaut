package ui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


import logger.Logger;
import logger.ILogConfig.Module;

import core.Constants;

public class Monitor extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private LoggingConsole console;
	private Logger logger;
	
	public Monitor(Logger logger) {
		
		this.logger = logger;
		console = new LoggingConsole();
		logger.addListener(console);
		
		Container pane = getContentPane();
		pane.add(console, BorderLayout.CENTER);
		pack();
		
		try{
			UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
			UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(480, 240);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
	}
	
	public void start() {
		console.clearConsole();
		setVisible(true);
	}
	
	public void stop() {
		setVisible(false);
	}
	
	public void dispose(){
		console.deregister();
		super.dispose();
	}
	
	public void log(String text) {
		logger.log(Module.COMMON, text);
	}
}
