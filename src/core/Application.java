package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import launch.LaunchManager;
import ui.Window;
import util.Logger;
import util.UiTools;
import util.Logger.Mode;

public class Application {

	private static Application instance;
	
	public static Application getInstance(){
		if(instance == null){
			instance = new Application();
		}
		return instance;
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
	
	private Logger logger;
	private Window window;
	private Configuration config;
	private History history;
	private Cache cache;
	private Registry registry;
	private PersistenceManager persistence;
	private TaskManager taskManager;
	private LaunchManager launchManager;
	
	public Logger getLogger(){ return logger; }
	public Window getWindow(){ return window; }
	public Configuration getConfig(){ return config; }
	public History getHistory(){ return history; }
	public Cache getCache(){ return cache; }
	public Registry getRegistry(){ return registry; }
	public PersistenceManager getPersistence(){ return persistence; }
	public TaskManager getTaskManager(){ return taskManager; }
	public LaunchManager getLaunchManager(){ return launchManager; }
	
	private Application(){}
	
	public void init() throws Exception {
		
		initPersistence();
		initSystems();
		initUI();
	}

	public void shutdown(){
		
		if(
			!launchManager.isBusy() ||
			UiTools.confirmDialog("Aboard running launches ?")
		){
			logger.info("Shutdown");
			try{
				shutdownUI();
				shutdownSystems();
				shutdownPersistence();
			}catch(Exception e){
				window.popupError(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
	}
	
	private void initPersistence() throws Exception {
		
		persistence = new PersistenceManager();
		persistence.init();
		
		File logFile = new File(persistence.getDataFolderPath()+File.separator+Logger.OUTPUT_FILE);
		logger = new Logger(Mode.FILE_AND_CONSOLE);
		logger.setLogiFile(logFile);
		logger.info(Constants.APP_FULL_NAME);
		
		File configFile = new File(persistence.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
		if(configFile.isFile()){
			config = Configuration.load(configFile.getAbsolutePath());
		}else{
			config = new Configuration(configFile.getAbsolutePath());
			config.save();
		}		
		Logger.VERBOSE = config.isVerbose();
		
		File historyFile = new File(persistence.getDataFolderPath()+File.separator+History.OUTPUT_FILE);
		if(historyFile.isFile()){
			history = History.load(historyFile.getAbsolutePath());
		}else{
			history = new History(historyFile.getAbsolutePath());
			history.save();
		}
		
		File cacheFile = new File(persistence.getDataFolderPath()+File.separator+Cache.OUTPUT_FILE);
		if(cacheFile.isFile()){
			cache = Cache.load(cacheFile.getAbsolutePath());
		}else{
			cache = new Cache(cacheFile.getAbsolutePath());
			cache.save();
		}
	}
	
	private void shutdownPersistence() throws Exception {
		
		config.chekForSave();
		persistence.shutdown();		
		// TODO clean cache
	}
	
	private void initSystems() {
		
		registry = new Registry();
		registry.init();
		
		taskManager = new TaskManager();
		taskManager.init();
		
		launchManager = new LaunchManager();
		launchManager.init();
	}
	
	private void shutdownSystems() {
		
		launchManager.shutdown();
		taskManager.shutdown();
	}
	
	private void initUI() throws Exception {
		
		window = new Window();
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){ shutdown(); }
        });
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
		SwingUtilities.updateComponentTreeUI(window);
		window.init();
	}
	
	private void shutdownUI(){
		
		window.dispose();
	}

	/** drop any unsaved changes */
	public void revert() throws Exception {
		
		if(config.isDirty()){
			shutdownUI();
			initPersistence();
			initUI();
		}
	}
}
