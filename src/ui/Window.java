package ui;

import http.IHttpServer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import launch.LaunchManager;
import launch.ScheduleManager;

import logger.Logger;
import logger.ILogConfig.Module;

import util.DateTools;
import util.IChangeListener;
import util.StringTools;
import util.Task;

import core.Configuration;
import core.Constants;
import core.HeapManager;
import core.ISystemComponent;
import core.TaskManager;
import core.HeapManager.HeapStatus;
import core.TaskManager.TaskInfo;

public class Window extends JFrame implements ISystemComponent, IStatusClient, IChangeListener {

	private static final long serialVersionUID = 1L;
	
	private Logger logger;
	private TaskManager taskManager;
	private HeapManager heapManager;
	private Configuration configuration;
	private ScheduleManager scheduleManager;
	private IHttpServer httpServer;
	private ProjectMenu projectMenu;
	
	private JLabel statusLabel;
	private JLabel infoLabel;
	
	private InfoUpdater updater;
	
	public Window(
			Logger logger,
			TaskManager taskManager,
			HeapManager heapManager,
			Configuration configuration,
			LaunchManager launchManager,
			ScheduleManager scheduleManager,
			IHttpServer httpServer,
			ProjectMenu projectMenu,
			ToolsMenu toolsMenu,
			ConfigPanel configPanel,
			SchedulerPanel schedulerPanel,
			HistoryPanel historyPanel,
			PreferencePanel preferencePanel)
	{
		this.logger = logger;
		this.taskManager = taskManager;
		this.heapManager = heapManager;
		this.configuration = configuration;
		this.scheduleManager = scheduleManager;
		this.httpServer = httpServer;
		this.projectMenu = projectMenu;
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(projectMenu);
		menuBar.add(toolsMenu);
		setJMenuBar(menuBar);
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(schedulerPanel, "Scheduler");
		centerPanel.add(historyPanel, "History");
		centerPanel.add(preferencePanel, "Preferences");
		
		statusLabel = new JLabel();
		statusLabel.setEnabled(false);
		infoLabel = new JLabel();
		infoLabel.setEnabled(false);
		
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(statusLabel, BorderLayout.CENTER);
		infoPanel.add(infoLabel, BorderLayout.EAST);
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(infoPanel, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
		
		configuration.addListener(this);
		launchManager.addClient(this);
		
		updater = null;
	}
	
	@Override
	public void init() throws Exception {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				projectMenu.quit(); 
			}
		});
		
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
		SwingUtilities.updateComponentTreeUI(this);
		
		setStatus(Constants.APP_NAME+" started at "+DateTools.getTextDate(new Date()));
		setInfo("");
		setVisible(true);
		
		if(updater == null){
			updater = new InfoUpdater();
			updater.asyncRun(0, 0);
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		
		if(updater != null){
			updater.syncKill();
			updater = null;
		}
		
		dispose();
	}
	
	public void setStatus(String text){
		statusLabel.setText(text);
		logger.log(Module.COMMON, text);
	}
	
	@Override
	public void status(String text) {
		setStatus(text);
	}
	
	public void setInfo(String text){
		infoLabel.setText(text);
	}

	@Override
	public void changed(Object object) {
		
		if(object == configuration){
			Configuration.State state = configuration.getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}
	}
	
	private class InfoUpdater extends Task {

		public static final long CYCLE = 3 * 1000; // 3 sec

		public InfoUpdater() {
			super("InfoUpdater", taskManager);
			setCycle(CYCLE);
		}

		@Override
		protected void runTask() {
			
			ArrayList<String> infos = new ArrayList<String>();
			infos.add(getSchedulerInfo());
			infos.add(getHttpServerInfo());
			infos.add(getTaskInfo());
			infos.add(getHeapInfo());
			setInfo(StringTools.join(infos, " | "));
		}
		
		public String getSchedulerInfo(){
			return "Scheduler "+(scheduleManager.isRunning() ? "ON" : "OFF");
		}
		
		public String getHttpServerInfo(){
			return "Server "+(httpServer.isRunning() ? "ON" : "OFF");
		}
		
		public String getTaskInfo(){
			ArrayList<TaskInfo> infos = taskManager.getInfo();
			return "Tasks ("+infos.size()+")";
		}
		
		public String getHeapInfo(){
			HeapStatus heap = heapManager.getHeapStatus();
			long MB = 1024 * 1024;
			return "HEAP ("+Math.round(heap.usedMemory / MB)+" / "+Math.round(heap.maxMemory / MB)+" MB)";
		}
	}
}
