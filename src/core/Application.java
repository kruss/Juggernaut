package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import operation.ConsoleOperationConfig;
import operation.SampleOperationConfig;

import lifecycle.LaunchManager;
import trigger.IntervallTriggerConfig;
import ui.Window;
import util.FileTools;
import util.Logger;
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
		
		initFolders();
		initLogger();
		initPersistence();
		initRegistry();
		initUI();
		initSystems();
	}

	public void shutdown(){
		
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
	
	private void initFolders() {

		File folder = new File(getOutputFolder());
		if(!folder.isDirectory()){
			folder.mkdirs();
		}
	}

	private void initLogger() {
		
		logger = new Logger(
				new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE)
		);
		logger.setVerbose(true);
		logger.info(Constants.APP_FULL_NAME);
	}
	
	private void initPersistence() throws Exception {
		
		File file = new File(getOutputFolder()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			configuration = Configuration.load(file.getAbsolutePath());
		}else{
			configuration = new Configuration(file.getAbsolutePath());
			configuration.save();
		}
	}
	
	private void shutdownPersistence() throws Exception {
		
		configuration.chekForSave();
	}
	
	private void initRegistry() {
		
		registry = new Registry();
		registry.getOperationConfigs().add(new SampleOperationConfig());
		registry.getOperationConfigs().add(new ConsoleOperationConfig());
		registry.getTriggerConfigs().add(new IntervallTriggerConfig());
	}
	
	private void initUI() throws Exception {
		
		window = new Window();
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
	
	private void initSystems() {
		
		launchManager = new LaunchManager();
		launchManager.init();
		configuration.notifyListeners();
	}
	
	private void shutdownSystems() {
		
		launchManager.shutdown();
	}
	
	public void handleException(Exception e){
		
		logger.error(e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	public String getOutputFolder(){
		return FileTools.getWorkingDir()+File.separator+Constants.OUTPUT_FOLDER;
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
