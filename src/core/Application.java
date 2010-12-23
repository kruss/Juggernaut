package core;

import http.HttpServer;
import launch.LaunchManager;
import launch.ScheduleManager;
import ui.ConfigPanel;
import ui.HistoryPanel;
import ui.PreferencePanel;
import ui.ProjectMenu;
import ui.SchedulerPanel;
import ui.ToolsMenu;
import ui.Window;

public class Application extends AbstractSystem implements IApplicationAdmin {

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
	
	private IOSystem ioSystem;
	private PersistenceSystem persistenceSystem;
	private RuntimeSystem runtimeSystem;
	private UISystem uiSystem;
	
	private Application(){}
	
	@Override
	public void init() throws Exception {
		
		ioSystem = new IOSystem();
		add(ioSystem);
		persistenceSystem = new PersistenceSystem();
		add(persistenceSystem);
		runtimeSystem = new RuntimeSystem();
		add(runtimeSystem);
		uiSystem = new UISystem();
		add(uiSystem);
		super.init();
	}
	
	@Override
	public void quit() throws Exception {
		shutdown();
	}
	
	@Override
	public void revert() throws Exception {
		
		if(isInitialized() && persistenceSystem.configuration.isDirty()){
			uiSystem.shutdown();
			persistenceSystem.clear();
			persistenceSystem.init();
			uiSystem.clear();
			uiSystem.init();
		}
	}
	
	/** io related components */
	private class IOSystem extends AbstractSystem {
		
		public SystemLogger logger;
		public FileManager fileManager;
		
		@Override
		public void init() throws Exception {
			
			logger = new SystemLogger();
			add(logger);
			fileManager = new FileManager(logger);
			add(fileManager);
			super.init();
		}
	}
	
	/** persistence related components */
	private class PersistenceSystem extends AbstractSystem {
		
		public Cache cache;
		public Configuration configuration;
		public History history;
		
		@Override
		public void init() throws Exception {
			
			cache = Cache.create(
					ioSystem.fileManager, 
					ioSystem.logger);
			add(cache);
			configuration = Configuration.create(
					cache, 
					ioSystem.fileManager, 
					runtimeSystem.taskManager, // TODO remove dependency
					ioSystem.logger);
			add(configuration);
			history = History.create(
					configuration, 
					ioSystem.fileManager, 
					ioSystem.logger);
			add(history);
			super.init();
			
			ioSystem.logger.setLogConfig(configuration);
		}
	}
	
	/** runtime related components */
	private class RuntimeSystem extends AbstractSystem {
		
		public TaskManager taskManager;
		public HeapManager heapManager;
		public Registry registry;
		public LaunchManager launchManager;
		public ScheduleManager scheduleManager;
		public HttpServer httpServer;
		
		@Override
		public void init() throws Exception {
			
			taskManager = new TaskManager(
					ioSystem.logger);
			add(taskManager);
			heapManager = new HeapManager(
					taskManager, 
					ioSystem.logger);
			add(heapManager);
			registry = new Registry(
					persistenceSystem.configuration, 
					persistenceSystem.cache, 
					taskManager, 
					ioSystem.logger);
			add(registry);
			launchManager = new LaunchManager(
					persistenceSystem.configuration);
			add(launchManager);
			scheduleManager = new ScheduleManager(
					persistenceSystem.configuration, 
					persistenceSystem.history, 
					ioSystem.fileManager, 
					taskManager, 
					launchManager, 
					ioSystem.logger);
			add(scheduleManager);
			httpServer = new HttpServer(
					Constants.HTTP_PORT, 
					ioSystem.fileManager, 
					taskManager, 
					ioSystem.logger);
			add(httpServer);
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
		public Window window;
		
		@Override
		public void init() throws Exception {
			
			projectMenu = new ProjectMenu(
					application, 
					persistenceSystem.configuration, 
					runtimeSystem.launchManager, 
					ioSystem.logger);
			add(projectMenu);
			toolsMenu = new ToolsMenu(
					persistenceSystem.configuration, 
					ioSystem.fileManager, 
					runtimeSystem.heapManager);
			add(toolsMenu);
			configPanel = new ConfigPanel(
					persistenceSystem.configuration, 
					persistenceSystem.history, 
					ioSystem.fileManager, 
					runtimeSystem.taskManager, 
					runtimeSystem.launchManager, 
					runtimeSystem.registry);
			add(configPanel);
			schedulerPanel = new SchedulerPanel(
					runtimeSystem.launchManager, 
					runtimeSystem.scheduleManager, 
					ioSystem.logger);
			add(schedulerPanel);
			historyPanel = new HistoryPanel(
					persistenceSystem.history, 
					ioSystem.logger);
			add(historyPanel);
			preferencePanel = new PreferencePanel(
					persistenceSystem.configuration, 
					runtimeSystem.scheduleManager, 
					persistenceSystem.history);
			add(preferencePanel);
			window = new Window(
					persistenceSystem.configuration, 
					runtimeSystem.launchManager, 
					runtimeSystem.heapManager, 
					ioSystem.logger, 
					projectMenu, 
					toolsMenu, 
					configPanel, 
					schedulerPanel, 
					historyPanel, 
					preferencePanel);
			add(window);
			super.init();
		}
	}
}
