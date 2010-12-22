package core;

import http.HttpServer;
import launch.LaunchManager;
import launch.ScheduleManager;
import logger.Logger;
import ui.ConfigPanel;
import ui.HistoryPanel;
import ui.PreferencePanel;
import ui.ProjectMenu;
import ui.SchedulerPanel;
import ui.ToolsMenu;
import ui.Window;

public class Application extends AbstractSystem implements IApplicationAdmin {

	private static Application application;
	
	public static Application getInstance(){
		if(application == null){
			application = new Application();
		}
		return application;
	}
	
	public static void main(String[] args){
		
		try{ 
			Application application = Application.getInstance();
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
	
	private SystemLogger logger;
	private Configuration configuration;
	private History history;
	private Cache cache;
	private Registry registry;
	private HeapManager heapManager;
	private FileManager fileManager;
	private TaskManager taskManager;
	private LaunchManager launchManager;
	private ScheduleManager scheduleManager;
	private HttpServer httpServer;
	
	public Logger getLogger(){ return logger; }
	//public Configuration getConfiguration(){ return configuration; }
	//public History getHistory(){ return history; }
	//public Cache getCache(){ return cache; }
	//public Registry getRegistry(){ return registry; }
	//public HeapManager getHeapManager(){ return heapManager; }
	//public FileManager getFileManager(){ return fileManager; }
	public TaskManager getTaskManager(){ return taskManager; }
	//public LaunchManager getLaunchManager(){ return launchManager; }
	//public ScheduleManager getScheduleManager(){ return scheduleManager; } 
	//public HttpServer getHttpServer(){ return httpServer; }
	
	private Application(){
		
		ioSystem = new IOSystem();
		add(ioSystem);
		persistenceSystem = new PersistenceSystem();
		add(persistenceSystem);
		runtimeSystem = new RuntimeSystem();
		add(runtimeSystem);
		uiSystem = new UISystem();
		add(uiSystem);
	}
	
	@Override
	public void revert() throws Exception {
		if(isInitialized() && configuration.isDirty()){
			uiSystem.shutdown();
			persistenceSystem.clear();
			persistenceSystem.init();
			uiSystem.clear();
			uiSystem.init();
		}
	}
	
	@Override
	public void quit() throws Exception {
		shutdown();
	}
	
	/** io related components */
	private class IOSystem extends AbstractSystem {
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
		@Override
		public void init() throws Exception {
			cache = Cache.create(fileManager, logger);
			add(cache);
			configuration = Configuration.create(cache, fileManager, logger);
			add(configuration);
			history = History.create(configuration, fileManager, logger);
			add(history);
			super.init();
			
			logger.setLogConfig(configuration);
		}
	}
	
	/** runtime related components */
	private class RuntimeSystem extends AbstractSystem {
		@Override
		public void init() throws Exception {
			heapManager = new HeapManager(logger);
			add(heapManager);
			registry = new Registry(configuration, cache);
			add(registry);
			taskManager = new TaskManager();
			add(taskManager);
			launchManager = new LaunchManager(configuration);
			add(launchManager);
			scheduleManager = new ScheduleManager(configuration, history, fileManager, launchManager, logger);
			add(scheduleManager);
			httpServer = new HttpServer(Constants.HTTP_PORT, fileManager.getHistoryFolder(), logger);
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
			projectMenu = new ProjectMenu(application, configuration, launchManager, logger);
			add(projectMenu);
			toolsMenu = new ToolsMenu(configuration, fileManager, heapManager);
			add(toolsMenu);
			configPanel = new ConfigPanel(configuration, history, fileManager, launchManager, registry);
			add(configPanel);
			schedulerPanel = new SchedulerPanel(launchManager, scheduleManager, logger);
			add(schedulerPanel);
			historyPanel = new HistoryPanel(history, logger);
			add(historyPanel);
			preferencePanel = new PreferencePanel(configuration, scheduleManager, history);
			add(preferencePanel);
			window = new Window(
					configuration, launchManager, heapManager, logger, 
					projectMenu, toolsMenu, 
					configPanel, schedulerPanel, historyPanel, preferencePanel);
			add(window);
			super.init();
		}
	}
}
