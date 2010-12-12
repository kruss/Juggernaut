package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import launch.LaunchManager;
import logger.Logger;
import logger.Logger.Mode;
import logger.Logger.Module;
import ui.Window;
import util.UiTools;

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
	
	private ArrayList<Component> components;
	private Persistence persistence;
	private Runtime runtime;
	private UI ui;
	
	private Logger logger;
	private Window window;
	private Configuration config;
	private History history;
	private Cache cache;
	private Registry registry;
	private FileManager fileManager;
	private TaskManager taskManager;
	private LaunchManager launchManager;
	
	public Logger getLogger(){ return logger; }
	public Window getWindow(){ return window; }
	public Configuration getConfig(){ return config; }
	public History getHistory(){ return history; }
	public Cache getCache(){ return cache; }
	public Registry getRegistry(){ return registry; }
	public FileManager getFileManager(){ return fileManager; }
	public TaskManager getTaskManager(){ return taskManager; }
	public LaunchManager getLaunchManager(){ return launchManager; }
	
	private Application(){
		
		persistence = new Persistence();
		runtime = new Runtime();
		ui = new UI();

		components = new ArrayList<Component>();
		components.add(persistence);
		components.add(runtime);
		components.add(ui);
	}
	
	public void init() throws Exception {
		
		for(int i=0; i<components.size(); i++){
			components.get(i).init();
		}
	}

	public void shutdown(){
		
		if(
			!launchManager.isBusy() ||
			UiTools.confirmDialog("Aboard running launches ?")
		){
			logger.info(Module.APP, "Shutdown");
			try{
				for(int i=components.size()-1; i>=0; i--){
					components.get(i).shutdown();
				}
			}catch(Exception e){
				popupError(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
	}

	/** drop any unsaved changes and restart ui */
	public void revert() throws Exception {
		if(config.isDirty()){
			ui.shutdown();
			persistence.init();
			ui.init();
		}
	}
	
	public void popupError(Exception e){
		logger.error(Module.APP, e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	abstract class Component {
		public abstract void init() throws Exception;
		public abstract void shutdown() throws Exception;
	}
	
	class Persistence extends Component {
		@Override
		public void init() throws Exception {
			fileManager = new FileManager();
			fileManager.init();

			File logFile = new File(fileManager.getDataFolderPath()+File.separator+Logger.OUTPUT_FILE);
			logger = new Logger(Mode.FILE_AND_CONSOLE);
			logger.setLogfile(logFile);
			logger.info(Module.APP, Constants.APP_FULL_NAME);

			File configFile = new File(fileManager.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
			if(configFile.isFile()){
				config = Configuration.load(configFile.getAbsolutePath());
			}else{
				config = new Configuration(configFile.getAbsolutePath());
				config.save();
			}		

			File historyFile = new File(fileManager.getDataFolderPath()+File.separator+History.OUTPUT_FILE);
			if(historyFile.isFile()){
				history = History.load(historyFile.getAbsolutePath());
			}else{
				history = new History(historyFile.getAbsolutePath());
				history.save();
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
			config.chekForSave();
			fileManager.shutdown();		
		}
	}
	
	class Runtime extends Component {
		@Override
		public void init() throws Exception {
			registry = new Registry();
			registry.init();
			taskManager = new TaskManager();
			taskManager.init();
			launchManager = new LaunchManager();
			launchManager.init();
		}
		@Override
		public void shutdown() throws Exception {
			launchManager.shutdown();
			taskManager.shutdown();
		}
	}
	
	class UI extends Component {
		@Override
		public void init() throws Exception {
			window = new Window();
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			window.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e){ 
	            	Application.getInstance().shutdown(); 
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
