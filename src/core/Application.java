package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import operation.ConsoleOperationConfig;
import operation.OperationRegistry;

import launch.LaunchManager;
import ui.MainFrame;
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
			Application app = Application.getInstance();
			app.init(); 
		}catch(Exception e){
			e.printStackTrace();
			System.exit(Constants.PROCESS_NOK);
		}
	}
	
	private MainFrame frame;
	private ConfigStore configStore;
	private OperationRegistry operationRegistry;
	private Logger logger;
	
	public Logger getLogger(){ return logger; }
	public MainFrame getFrame(){ return frame; }
	public ConfigStore getConfigStore(){ return configStore; }
	public OperationRegistry getOperationRegistry(){ return operationRegistry; }
	
	private Application(){}
	
	public void init() throws Exception {
		
		initFolder();
		initLogger();
		initPersistence();
		initRegistry();
		LaunchManager.getInstance().init();
		initFrame();
		logger.info(Constants.APP_FULL_NAME);
	}

	private void initFolder() {

		File folder = new File(getOutputFolder());
		if(!folder.isDirectory()){
			folder.mkdirs();
		}
	}

	private void initLogger() {
		
		logger = new Logger(
				new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE)
		);
	}
	
	private void initPersistence() throws Exception {
		
		File file = new File(getOutputFolder()+File.separator+ConfigStore.OUTPUT_FILE);
		if(file.isFile()){
			configStore = ConfigStore.load(file.getAbsolutePath());
		}else{
			configStore = new ConfigStore(file.getAbsolutePath());
			configStore.save();
		}
	}
	
	private void initRegistry() {
		
		operationRegistry = new OperationRegistry();
		operationRegistry.getOperationConfigs().add(new ConsoleOperationConfig());
	}
	
	private void initFrame() throws Exception {
		
		frame = new MainFrame();
		configStore.addListener(frame);
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){ shutdown(); }
        });
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[1].getClassName()); 
		SwingUtilities.updateComponentTreeUI(frame);
		frame.init();
	}
	
	public void shutdown(){
		
		logger.info("Shutdown");
		try{
			configStore.chekForSave();
		}catch(Exception e){
			handleException(e);
		}
		LaunchManager.getInstance().shutdown();
		System.exit(Constants.PROCESS_OK);
	}
	
	public void handleException(Exception e){
		
		logger.error(e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	private String getOutputFolder(){
		return FileTools.getWorkingDir()+File.separator+Constants.OUTPUT_FOLDER;
	}
}
