package core;

import http.HttpServer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import launch.LaunchManager;
import launch.ScheduleManager;
import logger.Logger;
import logger.Logger.Mode;
import logger.Logger.Module;
import ui.Window;
import util.UiTools;

public class Application implements ISystemComponent {

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
	
	private ArrayList<ISystemComponent> systems;
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
		runtimeSystem = new RuntimeSystem();
		uiSystem = new UiSystem();

		systems = new ArrayList<ISystemComponent>();
		systems.add(persistenceSystem);
		systems.add(runtimeSystem);
		systems.add(uiSystem);
	}
	
	@Override
	public void init() throws Exception {
		for(int i=0; i<systems.size(); i++){
			systems.get(i).init();
		}
	}

	@Override
	public void shutdown() throws Exception {
		for(int i=systems.size()-1; i>=0; i--){
			systems.get(i).shutdown();
		}
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
	
	private class RuntimeSystem implements ISystemComponent {
		@Override
		public void init() throws Exception {
			heapManager = new HeapManager(logger);
			heapManager.init();
			registry = new Registry();
			registry.init();
			taskManager = new TaskManager();
			taskManager.init();
			launchManager = new LaunchManager(application, configuration);
			launchManager.init();
			scheduleManager = new ScheduleManager(configuration, launchManager, logger);
			scheduleManager.init();
			httpServer = new HttpServer(
					Constants.HTTP_PORT, fileManager.getHistoryFolder(), logger
			);
			httpServer.init();
		}
		@Override
		public void shutdown() throws Exception {
			httpServer.shutdown();
			scheduleManager.shutdown();
			launchManager.shutdown();
			taskManager.shutdown();
			registry.shutdown();
			heapManager.shutdown();
		}
	}
	
	private class UiSystem implements ISystemComponent {
		@Override
		public void init() throws Exception {
			window = new Window();
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			window.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e){ 
	            	quit(); 
	            }
	        });
			UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
			UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
			SwingUtilities.updateComponentTreeUI(window);
			window.init();
		}
		@Override
		public void shutdown() throws Exception {
			window.dispose();
		}
	}
}
