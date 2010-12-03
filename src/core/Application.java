package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lifecycle.LaunchManager;
import ui.Window;
import util.PersistenceManager;
import util.Logger;
import util.SystemTools;
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
	private Configuration configuration;
	private History history;
	private Cache cache;
	private Registry registry;
	private LaunchManager launchManager;
	
	public Logger getLogger(){ return logger; }
	public Window getWindow(){ return window; }
	public Configuration getConfiguration(){ return configuration; }
	public History getHistory(){ return history; }
	public Cache getCache(){ return cache; }
	public Registry getRegistry(){ return registry; }
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
				error(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
	}
	
	private void initPersistence() throws Exception {
		
		ArrayList<File> folders = new ArrayList<File>();
		folders.add(new File(getDataFolder()));
		folders.add(new File(getBuildFolder()));
		folders.add(new File(getHistoryFolder()));
		folders.add(new File(getTempFolder()));
		PersistenceManager.initialize(folders);
		
		logger = new Logger(Mode.FILE_AND_CONSOLE);
		logger.setLogiFile(new File(getDataFolder()+File.separator+Logger.OUTPUT_FILE));
		logger.info(Constants.APP_FULL_NAME);
		
		File configurationFile = new File(getDataFolder()+File.separator+Configuration.OUTPUT_FILE);
		if(configurationFile.isFile()){
			configuration = Configuration.load(configurationFile.getAbsolutePath());
		}else{
			configuration = new Configuration(configurationFile.getAbsolutePath());
			configuration.save();
		}		
		Logger.VERBOSE = configuration.isVerbose();
		
		File historyFile = new File(getDataFolder()+File.separator+History.OUTPUT_FILE);
		if(historyFile.isFile()){
			history = History.load(historyFile.getAbsolutePath());
		}else{
			history = new History(historyFile.getAbsolutePath());
			history.save();
		}
		
		File cacheFile = new File(getDataFolder()+File.separator+Cache.OUTPUT_FILE);
		if(cacheFile.isFile()){
			cache = Cache.load(cacheFile.getAbsolutePath());
		}else{
			cache = new Cache(cacheFile.getAbsolutePath());
			cache.save();
		}
	}
	
	private void shutdownPersistence() throws Exception {
		
		configuration.chekForSave();
		PersistenceManager.cleanup(configuration, new File(getBuildFolder()), logger);
		PersistenceManager.delete(new File(getTempFolder()), logger);
	}
	
	private void initSystems() {
		
		registry = new Registry();
		registry.init();
		
		launchManager = new LaunchManager();
		launchManager.init();
	}
	
	private void shutdownSystems() {
		
		launchManager.shutdown();
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
	
	public void error(Exception e){
		
		logger.error(e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	public String getDataFolder(){
		return SystemTools.getWorkingDir()+File.separator+Constants.DATA_FOLDER;
	}
	
	public String getBuildFolder(){
		return SystemTools.getWorkingDir()+File.separator+Constants.BUILD_FOLDER;
	}
	
	public String getHistoryFolder(){
		return SystemTools.getWorkingDir()+File.separator+Constants.HISTORY_FOLDER;
	}
	
	public String getTempFolder(){
		return SystemTools.getWorkingDir()+File.separator+Constants.TEMP_FOLDER;
	}

	/** drop any unsaved changes */
	public void revert() throws Exception {
		
		if(configuration.isDirty()){
			shutdownUI();
			initPersistence();
			initUI();
		}
	}
}
