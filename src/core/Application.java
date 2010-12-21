package core;

import http.HttpServer;

import java.io.File;

import launch.LaunchManager;
import launch.ScheduleManager;
import logger.Logger;
import logger.Logger.Mode;
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
	
	private PersistenceSystem persistenceSystem;
	private RuntimeSystem runtimeSystem;
	private UiSystem uiSystem;
	
	private Logger logger;
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
		
		persistenceSystem = new PersistenceSystem();
		add(persistenceSystem);
		runtimeSystem = new RuntimeSystem();
		add(runtimeSystem);
		uiSystem = new UiSystem();
		add(uiSystem);
	}
	
	public void quit() {
		
		if(
			!launchManager.isBusy() ||
			UiTools.confirmDialog("Aboard running launches ?")
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

	/** drop any unsaved changes and restart ui */
	public void revert() throws Exception {
		if(configuration.isDirty()){
			uiSystem.shutdown();
			persistenceSystem.init();
			uiSystem.init();
		}
	}
	
	public void popupError(Exception e){
		logger.error(Module.COMMON, e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	public void setStatus(String text) {
		if(window != null){
			window.setStatus(text);
		}
	}
	
	private class PersistenceSystem implements ISystemComponent {
		@Override
		public void init() throws Exception {
			fileManager = new FileManager();
			fileManager.init();

			File logFile = new File(fileManager.getDataFolderPath()+File.separator+Logger.OUTPUT_FILE);
			logger = new Logger(Mode.FILE_AND_CONSOLE);
			logger.setLogfile(logFile, Constants.LOGFILE_MAX);
			logger.info(Module.COMMON, Constants.APP_FULL_NAME);

			File configFile = new File(fileManager.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
			if(configFile.isFile()){
				configuration = Configuration.load(configFile.getAbsolutePath());
			}else{
				configuration = new Configuration(configFile.getAbsolutePath());
				configuration.save();
			}		

			File historyFile = new File(fileManager.getDataFolderPath()+File.separator+History.OUTPUT_FILE);
			if(historyFile.isFile()){
				history = History.load(historyFile.getAbsolutePath());
			}else{
				history = new History(historyFile.getAbsolutePath());
				history.save();
				history.createIndex();
			}

			File cacheFile = new File(fileManager.getDataFolderPath()+File.separator+Cache.OUTPUT_FILE);
			if(cacheFile.isFile()){
				cache = Cache.load(cacheFile.getAbsolutePath());
			}else{
				cache = new Cache(cacheFile.getAbsolutePath());
				cache.save();
			}
		}
		@Override
		public void shutdown() throws Exception {
			cache.clean();
			configuration.chekForSave();
			fileManager.shutdown();		
		}
	}
	
	private class RuntimeSystem extends AbstractSystem {
		@Override
		public void init() throws Exception {
			clear();
			heapManager = new HeapManager(logger);
			add(heapManager);
			registry = new Registry();
			add(registry);
			taskManager = new TaskManager();
			add(taskManager);
			launchManager = new LaunchManager(application, configuration);
			add(launchManager);
			scheduleManager = new ScheduleManager(configuration, launchManager, logger);
			add(scheduleManager);
			httpServer = new HttpServer(Constants.HTTP_PORT, fileManager.getHistoryFolder(), logger);
			add(httpServer);
			super.init();
		}
	}
	
	private class UiSystem extends AbstractSystem {
		@Override
		public void init() throws Exception {
			clear();
			window = new Window();
			add(window);
			super.init();
		}
	}
}
