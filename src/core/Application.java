package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import operation.ConsoleOperationConfig;
import operation.SampleOperationConfig;

import lifecycle.LaunchManager;
import trigger.IntervallTriggerConfig;
import ui.Window;
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
	private Registry registry;
	private LaunchManager launchManager;
	
	public Logger getLogger(){ return logger; }
	public Window getWindow(){ return window; }
	public Configuration getConfiguration(){ return configuration; }
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
				handleException(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
	}
	
	private void initPersistence() throws Exception {
		
		File folder = new File(getOutputFolder());
		if(!folder.isDirectory()){
			folder.mkdirs();
		}
		// TODO clean legacy stuff
		
		logger = new Logger(Mode.FILE_AND_CONSOLE);
		logger.setLogiFile(new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE));
		logger.info(Constants.APP_FULL_NAME);
		
		File file = new File(getOutputFolder()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			configuration = Configuration.load(file.getAbsolutePath());
		}else{
			configuration = new Configuration(file.getAbsolutePath());
			configuration.save();
		}
		
		Logger.VERBOSE = configuration.isVerbose();
	}
	
	private void shutdownPersistence() throws Exception {
		
		configuration.chekForSave();
	}
	
	private void initSystems() {
		
		registry = new Registry();
		registry.getOperationConfigs().add(new SampleOperationConfig());
		registry.getOperationConfigs().add(new ConsoleOperationConfig());
		registry.getTriggerConfigs().add(new IntervallTriggerConfig());
		
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
		UIManager.setLookAndFeel(styles[1].getClassName()); 
		SwingUtilities.updateComponentTreeUI(window);
		window.init();
	}
	
	private void shutdownUI(){
		
		window.dispose();
	}
	
	public void handleException(Exception e){
		
		logger.error(e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	public String getOutputFolder(){
		return SystemTools.getWorkingDir()+File.separator+Constants.OUTPUT_FOLDER;
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
