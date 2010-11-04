package ui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import core.Application;
import core.Constants;

public class Frame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JMenuBar menuBar;
	private JTabbedPane tabPane;
	private JPanel configPane;
	private JPanel statusPane;
	private JPanel historyPane;
	private JLabel statusBar;
	
	public Frame(){
		
		menuBar = new JMenuBar();
		menuBar.add(new LaunchMenu());
		setJMenuBar(menuBar);
		
		configPane = new ConfigPane();
		statusPane = new StatusPane();
		historyPane = new HistoryPane();
		
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.TOP);
		tabPane.add(configPane, "Config");
		tabPane.add(statusPane, "Status");
		tabPane.add(historyPane, "History");
		
		statusBar = new JLabel("> ");
		
		Container pane = getContentPane();
		pane.add(tabPane, BorderLayout.CENTER);
		pane.add(statusBar, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		
		setTitle(Constants.APP_NAME+" - "+Constants.APP_VERSION);
	}
	
	public void setStatus(String text){
		
		text = "> "+text;
		statusBar.setText(text);
		Application.getInstance().getLogger().log(text);
	}
}
