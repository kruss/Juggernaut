package ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import logger.Logger.Module;

import util.IChangedListener;
import util.StringTools;
import util.HeapManager.HeapStatus;

import core.Application;
import core.Configuration;
import core.Constants;

public class Window extends JFrame implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JMenuBar menuBar;
	private ConfigPanel configPanel;
	private SchedulerPanel schedulerPanel;
	private HistoryPanel historyPanel;
	private PreferencePanel preferencePanel;
	private JLabel statusBar;
	private JLabel heapInfo;
	
	public Window(){
		
		application = Application.getInstance();
		
		menuBar = new JMenuBar();
		menuBar.add(new ProjectMenu());
		menuBar.add(new ToolsMenu());
		setJMenuBar(menuBar);
		
		statusBar = new JLabel();
		statusBar.setEnabled(false);
		heapInfo = new JLabel();
		heapInfo.setEnabled(false);
		
		configPanel = new ConfigPanel();
		schedulerPanel = new SchedulerPanel();
		historyPanel = new HistoryPanel();
		preferencePanel = new PreferencePanel();
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(schedulerPanel, "Scheduler");
		centerPanel.add(historyPanel, "History");
		centerPanel.add(preferencePanel, "Preferences");
		
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(statusBar, BorderLayout.CENTER);
		infoPanel.add(heapInfo, BorderLayout.EAST);
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(infoPanel, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
		
		application.getConfig().addListener(this);
		application.getHeapManager().addListener(this);
	}
	
	public void init() {
		
		configPanel.init();
		schedulerPanel.init();
		historyPanel.init();
		preferencePanel.init();
		setStatus(Constants.APP_NAME+" started at "+StringTools.getTextDate(new Date()));
		setHeapStatus(application.getHeapManager().getHeapStatus());
		setVisible(true);
	}
	
	public void setStatus(String text){
		
		statusBar.setText(text);
		application.getLogger().log(Module.COMMON, text);
	}
	
	public void setHeapStatus(HeapStatus status){
		
		long MB = 1024 * 1024;
		String info = Math.round(status.usedMemory / MB)+" / "+Math.round(status.maxMemory / MB)+" MB";
		heapInfo.setText(info);
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getConfig()){
			Configuration.State state = application.getConfig().getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}else if(object == application.getHeapManager()){
			setHeapStatus(application.getHeapManager().getHeapStatus());
		}
	}
}
