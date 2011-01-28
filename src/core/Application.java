package core;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.FileManager;
import core.runtime.HeapManager;
import core.runtime.LaunchManager;
import core.runtime.Registry;
import core.runtime.ScheduleManager;
import core.runtime.TaskManager;
import core.runtime.http.HttpServer;
import core.runtime.logger.SystemLogger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.SmtpClient;
import ui.Window;
import ui.menu.HelpMenu;
import ui.menu.ProjectMenu;
import ui.menu.ToolsMenu;
import ui.panel.ConfigPanel;
import ui.panel.HistoryPanel;
import ui.panel.LoggerPanel;
import ui.panel.PreferencePanel;
import ui.panel.SchedulerPanel;

public class Application extends AbstractSystem {

	private static Application application;
	
	public static void main(String[] args){
		
		try{
			application = new Application();
			application.init(); 
		}catch(Exception e){
			e.printStackTrace();
			System.exit(Constants.PROCESS_NOK);
		}
	}
	
	private LogSystem logging;
	private CoreSystem core;
	private PersistenceSystem persistence;
	private RuntimeSystem runtime;
	private UISystem ui;
	
	private Application(){}
	
	@Override
	public void init() throws Exception {
		
		logging = new LogSystem();
		add(logging);
		core = new CoreSystem();
		add(core);
		persistence = new PersistenceSystem();
		add(persistence);
		runtime = new RuntimeSystem();
		add(runtime);
		ui = new UISystem();
		add(ui);
		super.init();
	}

	public void revert() throws Exception {
		
		if(ui.isInitialized() && persistence.configuration.isDirty()){
			logging.logger.emph(Module.COMMON, "revert");
			ui.shutdown();
			persistence.clear();
			persistence.init();
			ui.clear();
			ui.init();
			runtime.reverted();
		}
	}
	
	/** the application's logger */
	private class LogSystem extends AbstractSystem {
		
		public SystemLogger logger;
		
		@Override
		public void init() throws Exception {
			
			logger = new SystemLogger();
			add(logger);
			super.init();
		}
	}
	
	/** io related components */
	private class CoreSystem extends AbstractSystem {
		
		public TaskManager taskManager;
		public FileManager fileManager;
		public HeapManager heapManager;
		
		@Override
		public void init() throws Exception {
			
			taskManager = new TaskManager(
					logging.logger);
			add(taskManager);
			fileManager = new FileManager(
					taskManager);
			add(fileManager);
			heapManager = new HeapManager(
					taskManager, 
					logging.logger);
			add(heapManager);
			super.init();
		}
	}
	
	/** persistence related components */
	private class PersistenceSystem extends AbstractSystem {
		
		public Configuration configuration;
		public Cache cache;
		public History history;
		
		@Override
		public void init() throws Exception {
			
			configuration = Configuration.create(
					core.taskManager,
					core.fileManager, 
					logging.logger);
			add(configuration);
			cache = Cache.create(
					configuration,
					core.fileManager, 
					logging.logger);
			add(cache);
			history = History.create(
					configuration, 
					core.fileManager, 
					logging.logger);
			add(history);
			super.init();
		}
	}
	
	/** runtime related components */
	private class RuntimeSystem extends AbstractSystem {
		
		public Registry registry;
		public SmtpClient smtpClient;
		public LaunchManager launchManager;
		public ScheduleManager scheduleManager;
		public HttpServer httpServer;
		
		@Override
		public void init() throws Exception {
			
			registry = new Registry(
					core.taskManager, 
					logging.logger);
			add(registry);
			smtpClient = new SmtpClient(
					persistence.configuration);
			add(smtpClient);
			httpServer = new HttpServer(
					persistence.configuration, 
					core.fileManager, 
					core.taskManager, 
					logging.logger);
			add(httpServer);
			launchManager = new LaunchManager(
					persistence.configuration,
					core.fileManager,
					logging.logger);
			add(launchManager);
			scheduleManager = new ScheduleManager(
					persistence.configuration, 
					persistence.cache,
					persistence.history, 
					core.fileManager, 
					core.taskManager,
					smtpClient,
					httpServer,
					launchManager, 
					logging.logger);
			add(scheduleManager);
			super.init();
		}

		public void reverted() {
			launchManager.notifyListeners();
		}
	}
	
	/** ui related componets */
	private class UISystem extends AbstractSystem {
		
		public ProjectMenu projectMenu;
		public ToolsMenu toolsMenu;
		public HelpMenu helpMenu;
		public ConfigPanel configPanel;
		public SchedulerPanel schedulerPanel;
		public HistoryPanel historyPanel;
		public PreferencePanel preferencePanel;
		public LoggerPanel loggerPanel;
		public Window window;
		
		@Override
		public void init() throws Exception {
			
			projectMenu = new ProjectMenu(
					application, 
					persistence.configuration, 
					runtime.launchManager, 
					logging.logger);
			add(projectMenu);
			toolsMenu = new ToolsMenu(
					application,
					persistence.configuration,
					persistence.cache, 
					runtime.registry,
					core.taskManager,
					core.fileManager, 
					core.heapManager,
					logging.logger);
			add(toolsMenu);
			helpMenu = new HelpMenu(core.fileManager);
			add(helpMenu);
			configPanel = new ConfigPanel(
					persistence.configuration, 
					persistence.cache,
					persistence.history, 
					core.fileManager, 
					core.taskManager, 
					runtime.smtpClient,
					runtime.httpServer,
					runtime.launchManager, 
					runtime.registry,
					logging.logger);
			add(configPanel);
			schedulerPanel = new SchedulerPanel(
					runtime.launchManager, 
					runtime.scheduleManager, 
					logging.logger);
			add(schedulerPanel);
			historyPanel = new HistoryPanel(
					persistence.history, 
					logging.logger);
			add(historyPanel);
			preferencePanel = new PreferencePanel(
					persistence.configuration, 
					runtime.scheduleManager, 
					runtime.httpServer);
			add(preferencePanel);
			loggerPanel = new LoggerPanel(
					persistence.configuration, 
					logging.logger);
			add(loggerPanel);
			window = new Window(
					logging.logger, 
					core.taskManager, 
					core.heapManager, 
					persistence.configuration, 
					runtime.launchManager, 
					runtime.scheduleManager,
					runtime.httpServer,
					projectMenu, 
					toolsMenu,
					helpMenu,
					configPanel, 
					schedulerPanel, 
					historyPanel, 
					loggerPanel,
					preferencePanel);
			add(window);
			super.init();
		}
	}
}
