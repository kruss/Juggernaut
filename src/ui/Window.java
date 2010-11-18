package ui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import core.Application;
import core.Configuration;
import core.Constants;
import core.IChangeListener;

public class Window extends JFrame implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private JMenuBar menuBar;
	private ConfigPanel configPanel;
	private StatusPanel statusPanel;
	private HistoryPanel historyPanel;
	private PreferencePanel preferencePanel;
	private JLabel statusBar;
	
	public Window(){
		
		menuBar = new JMenuBar();
		menuBar.add(new ProjectMenu());
		setJMenuBar(menuBar);
		
		statusBar = new JLabel();
		statusBar.setEnabled(false);
		
		configPanel = new ConfigPanel();
		statusPanel = new StatusPanel();
		historyPanel = new HistoryPanel();
		preferencePanel = new PreferencePanel();
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(statusPanel, "Status");
		centerPanel.add(historyPanel, "History");
		centerPanel.add(preferencePanel, "Preferences");
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(statusBar, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		
		setTitle(Constants.APP_FULL_NAME);
	}
	
	public void init() {
		
		configPanel.init();
		statusPanel.init();
		historyPanel.init();
		preferencePanel.init();
		setVisible(true);
	}
	
	public void setStatus(String text){
		
		statusBar.setText(text);
		Application.getInstance().getLogger().log("> "+text);
	}

	@Override
	public void changed(Object object) {
		
		if(object == Application.getInstance().getConfiguration()){
			Configuration.State state = Application.getInstance().getConfiguration().getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}
	}
}
