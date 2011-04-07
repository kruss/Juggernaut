package ui;

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



import ui.menu.HelpMenu;
import ui.menu.ProjectMenu;
import ui.menu.ToolsMenu;
import ui.panel.ConfigPanel;
import ui.panel.HistoryPanel;
import ui.panel.LoggerPanel;
import ui.panel.PreferencePanel;
import ui.panel.SchedulerPanel;
import util.DateTools;
import util.IChangeListener;
import util.StringTools;
import util.SystemTools;
import util.UiTools;

import core.Constants;
import core.ISystemComponent;
import core.persistence.Configuration;
import core.runtime.HeapManager;
import core.runtime.LaunchManager;
import core.runtime.ScheduleManager;
import core.runtime.TaskManager;
import core.runtime.HeapManager.HeapStatus;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ErrorManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class Window extends JFrame implements ISystemComponent, IStatusClient, IChangeListener {

	private static final long serialVersionUID = 1L;
	
	private Logger logger;
	private ErrorManager errorManager;
	private TaskManager taskManager;
	private HeapManager heapManager;
	private Configuration configuration;
	private ScheduleManager scheduleManager;
	private IHttpServer httpServer;
	private ProjectMenu projectMenu;
	
	private JLabel statusLabel;
	private JLabel infoLabel;
	
	public Window(
			Logger logger,
			ErrorManager errorManager,
			TaskManager taskManager,
			HeapManager heapManager,
			Configuration configuration,
			LaunchManager launchManager,
			ScheduleManager scheduleManager,
			IHttpServer httpServer,
			ProjectMenu projectMenu,
			ToolsMenu toolsMenu,
			HelpMenu helpMenu, 
			ConfigPanel configPanel,
			SchedulerPanel schedulerPanel,
			HistoryPanel historyPanel,
			LoggerPanel loggerPanel,
			PreferencePanel preferencePanel)
	{
		this.logger = logger;
		this.errorManager = errorManager;
		this.taskManager = taskManager;
		this.heapManager = heapManager;
		this.configuration = configuration;
		this.scheduleManager = scheduleManager;
		this.httpServer = httpServer;
		this.projectMenu = projectMenu;
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(projectMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(schedulerPanel, "Scheduler");
		centerPanel.add(loggerPanel, "Logger");
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
		setTitle();
		
		errorManager.addListener(this);
		configuration.addListener(this);
		scheduleManager.addListener(this);
		httpServer.addListener(this);
		taskManager.addListener(this);
		heapManager.addListener(this);
		launchManager.setStatusClient(this);
		configPanel.setStatusClient(this);
		preferencePanel.getEditor().setStatusClient(this);
	}
	
	@Override
	public void init() throws Exception {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				projectMenu.quit(); 
			}
		});
		
		UiTools.setStyle(this, Constants.APP_STYLE);
		
		setStatus(Constants.APP_NAME+" started at "+DateTools.getTextDate(new Date()));
		setInfo();
		setVisible(true);
	}
	
	@Override
	public void shutdown() throws Exception {
		dispose();
	}
	
	public void setTitle(){
		
		String title = Constants.APP_FULL_NAME+" - "+SystemTools.getWorkingDir()+(configuration.isDirty() ? " *" : "");
		setTitle(title);
	}
	
	public void setStatus(String text){
		statusLabel.setText(text);
		logger.debug(Module.COMMON, text);
	}
	
	@Override
	public void status(String text) {
		setStatus(text);
	}

	@Override
	public void changed(Object object) {
		
		if(object == configuration){
			setTitle();
		}
		
		if(
				object == errorManager || object == scheduleManager || object == httpServer || 
				object == taskManager || object == heapManager
		){
			setInfo();
		}
	}
	
	private void setInfo() {
		
		ArrayList<String> infos = new ArrayList<String>();
		infos.add("Schedule "+(scheduleManager.isRunning() ? "ON" : "OFF"));
		infos.add("Server "+(httpServer.isRunning() ? "ON" : "OFF"));
		infos.add("Task "+taskManager.getTaskCount());
		infos.add("Error "+errorManager.getErrorCount());
		HeapStatus heap = heapManager.getHeapStatus();
		long MB = 1024 * 1024;
		infos.add("Heap "+Math.round(heap.usedMemory / MB)+"/"+Math.round(heap.maxMemory / MB));
		infoLabel.setText(StringTools.join(infos, " | "));
	}
}
