package core;

import http.HttpServer;
import launch.LaunchManager;
import launch.ScheduleManager;
import logger.Logger;
import logger.Logger.Module;
import ui.Window;
import util.UiTools;

public class Application extends AbstractSystem {

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
	private Window window;
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
	public Window getWindow(){ return window; }
	public Configuration getConfiguration(){ return configuration; }
	public History getHistory(){ return history; }
	public Cache getCache(){ return cache; }
	public Registry getRegistry(){ return registry; }
	public HeapManager getHeapManager(){ return heapManager; }
	public FileManager getFileManager(){ return fileManager; }
	public TaskManager getTaskManager(){ return taskManager; }
	public LaunchManager getLaunchManager(){ return launchManager; }
	public ScheduleManager getScheduleManager(){ return scheduleManager; } 
	public HttpServer getHttpServer(){ return httpServer; }
	
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
	
	/** drop any unsaved changes and restart ui */
	public void revert() throws Exception {
		if(isInitialized() && configuration.isDirty()){
			uiSystem.shutdown();
			persistenceSystem.clear();
			persistenceSystem.init();
			uiSystem.clear();
			uiSystem.init();
		}
	}
	
	/** display an error-dialog */
	public void popupError(Exception e){
		if(isInitialized()){
			logger.error(Module.COMMON, e);
			UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
		}
	}
	
	/** quit the application */
	public void quit() {
		
		if(
			isInitialized() &&
			( 
				!launchManager.isBusy() || 
				UiTools.confirmDialog("Aboard running launches ?") 
			)
		){
			logger.info(Module.COMMON, "Shutdown");
			try{
				shutdown();
			}catch(Exception e){
				popupError(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
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
			configuration = Configuration.create(fileManager);
			add(configuration);
			history = History.create(fileManager);
			add(history);
			cache = Cache.create(fileManager);
			add(cache);
			super.init();
		}
	}
	
	/** runtime related components */
	private class RuntimeSystem extends AbstractSystem {
		@Override
		public void init() throws Exception {
			heapManager = new HeapManager(logger);
			add(heapManager);
			registry = new Registry();
			add(registry);
			taskManager = new TaskManager();
			add(taskManager);
			launchManager = new LaunchManager(configuration);
			add(launchManager);
			scheduleManager = new ScheduleManager(configuration, launchManager, logger);
			add(scheduleManager);
			httpServer = new HttpServer(Constants.HTTP_PORT, fileManager.getHistoryFolder(), logger);
			add(httpServer);
			super.init();
		}
	}
	
	/** ui related componets */
	private class UISystem extends AbstractSystem {
		@Override
		public void init() throws Exception {
			window = new Window(application);
			add(window);
			super.init();
		}
	}
}
