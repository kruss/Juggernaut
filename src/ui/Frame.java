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
import core.IConfigStoreListener;

public class Frame extends JFrame implements IConfigStoreListener {

	private static final long serialVersionUID = 1L;

	private JMenuBar menuBar;
	private JTabbedPane tabPane;
	private JPanel configPane;
	private JPanel statusPane;
	private JPanel historyPane;
	private JPanel optionsPane;
	private JLabel statusBar;
	
	public Frame(){
		
		menuBar = new JMenuBar();
		menuBar.add(new ProjectMenu());
		setJMenuBar(menuBar);
		
		configPane = new ConfigPane();
		statusPane = new StatusPane();
		historyPane = new HistoryPane();
		optionsPane = new OptionsPane();
		
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.TOP);
		tabPane.add(configPane, "Config");
		tabPane.add(statusPane, "Status");
		tabPane.add(historyPane, "History");
		tabPane.add(optionsPane, "Options");
		
		statusBar = new JLabel("> ");
		
		Container pane = getContentPane();
		pane.add(tabPane, BorderLayout.CENTER);
		pane.add(statusBar, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		
		setTitle(Constants.APP_FULL_NAME);
	}
	
	public void setStatus(String text){
		
		text = "> "+text;
		statusBar.setText(text);
		Application.getInstance().getLogger().log(text);
	}

	@Override
	public void configChanged(State state) {
		
		if(state == State.CLEAN){
			setTitle(Constants.APP_FULL_NAME);
		}else if(state == State.DIRTY){
			setTitle(Constants.APP_FULL_NAME+" *");
		}
	}
}
