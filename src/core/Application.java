package core;

import http.HttpServer;
import launch.LaunchManager;
import launch.ScheduleManager;
import smtp.SmtpClient;
import ui.ConfigPanel;
import ui.HistoryPanel;
import ui.LoggerPanel;
import ui.PreferencePanel;
import ui.ProjectMenu;
import ui.SchedulerPanel;
import ui.ToolsMenu;
import ui.Window;

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
	
	private CoreSystem core;
	private PersistenceSystem persistence;
	private RuntimeSystem runtime;
	private UISystem ui;
	
	private Application(){}
	
	@Override
	public void init() throws Exception {
		
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
			ui.shutdown();
			persistence.clear();
			persistence.init();
			ui.clear();
			ui.init();
		}
	}
	
	/** io related components */
	private class CoreSystem extends AbstractSystem {
		
		public SystemLogger logger;
		public TaskManager taskManager;
		public FileManager fileManager;
		public HeapManager heapManager;
		
		@Override
		public void init() throws Exception {
			
			logger = new SystemLogger();
			add(logger);
			taskManager = new TaskManager(
					logger);
			add(taskManager);
			fileManager = new FileManager(
					taskManager);
			add(fileManager);
			heapManager = new HeapManager(
					taskManager, 
					logger);
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
					core.fileManager, 
					core.taskManager,
					core.logger);
			add(configuration);
			cache = Cache.create(
					configuration,
					core.fileManager, 
					core.logger);
			add(cache);
			history = History.create(
					configuration, 
					core.fileManager, 
					core.logger);
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
					core.logger);
			add(registry);
			smtpClient = new SmtpClient(
					persistence.configuration);
			add(smtpClient);
			httpServer = new HttpServer(
					persistence.configuration, 
					core.fileManager, 
					core.taskManager, 
					core.logger);
			add(httpServer);
			launchManager = new LaunchManager(
					persistence.configuration);
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
					core.logger);
			add(scheduleManager);
			super.init();
		}
	}
	
	/** ui related componets */
	private class UISystem extends AbstractSystem {
		
		public ProjectMenu projectMenu;
		public ToolsMenu toolsMenu;
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
					core.logger);
			add(projectMenu);
			toolsMenu = new ToolsMenu(
					application,
					persistence.configuration, 
					core.taskManager,
					core.fileManager, 
					core.heapManager,
					core.logger);
			add(toolsMenu);
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
					core.logger);
			add(configPanel);
			schedulerPanel = new SchedulerPanel(
					runtime.launchManager, 
					runtime.scheduleManager, 
					core.logger);
			add(schedulerPanel);
			historyPanel = new HistoryPanel(
					persistence.history, 
					core.logger);
			add(historyPanel);
			preferencePanel = new PreferencePanel(
					persistence.configuration, 
					runtime.scheduleManager, 
					runtime.httpServer);
			add(preferencePanel);
			loggerPanel = new LoggerPanel(persistence.configuration, core.logger);
			add(loggerPanel);
			window = new Window(
					core.logger, 
					core.taskManager, 
					core.heapManager, 
					persistence.configuration, 
					runtime.launchManager, 
					runtime.scheduleManager,
					runtime.httpServer,
					projectMenu, 
					toolsMenu, 
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
