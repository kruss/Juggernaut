package ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import logger.Logger.Module;

import util.IChangedListener;
import util.StringTools;

import core.Application;
import core.Configuration;
import core.Constants;
import core.ISystemComponent;
import core.HeapManager.HeapStatus;

public class Window extends JFrame implements ISystemComponent, IStatusClient, IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JMenuBar menuBar;
	private ConfigPanel configPanel;
	private SchedulerPanel schedulerPanel;
	private HistoryPanel historyPanel;
	private PreferencePanel preferencePanel;
	private JLabel statusInfo;
	private JLabel heapInfo;
	
	public Window(Application application){
		
		this.application = application;
		
		menuBar = new JMenuBar();
		menuBar.add(new ProjectMenu());
		menuBar.add(new ToolsMenu());
		setJMenuBar(menuBar);
		
		statusInfo = new JLabel();
		statusInfo.setEnabled(false);
		heapInfo = new JLabel();
		heapInfo.setToolTipText("HEAP");
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
		infoPanel.add(statusInfo, BorderLayout.CENTER);
		infoPanel.add(heapInfo, BorderLayout.EAST);
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(infoPanel, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
		
		application.getConfiguration().addListener(this);
		application.getHeapManager().addListener(this);
		application.getLaunchManager().addClient(this);
	}
	
	@Override
	public void init() throws Exception {
		
		configPanel.init();
		schedulerPanel.init();
		historyPanel.init();
		preferencePanel.init();
		
		setStatus(Constants.APP_NAME+" started at "+StringTools.getTextDate(new Date()));
		setHeap(application.getHeapManager().getHeapStatus());
		
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
		SwingUtilities.updateComponentTreeUI(this);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				application.quit(); 
			}
		});
		setVisible(true);
	}
	
	@Override
	public void shutdown() throws Exception {
		dispose();
	}
	
	public void setStatus(String text){
		
		statusInfo.setText(text);
		application.getLogger().log(Module.COMMON, text);
	}
	
	@Override
	public void status(String text) {
		setStatus(text);
	}
	
	public void setHeap(HeapStatus status){
		
		long MB = 1024 * 1024;
		String info = Math.round(status.usedMemory / MB)+" / "+Math.round(status.maxMemory / MB)+" MB";
		heapInfo.setText(info);
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getConfiguration()){
			Configuration.State state = application.getConfiguration().getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}else if(object == application.getHeapManager()){
			setHeap(application.getHeapManager().getHeapStatus());
		}
	}
}
