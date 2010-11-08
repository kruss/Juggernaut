package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import launch.LaunchManager;
import ui.Frame;
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
	
	private Frame frame;
	private ConfigStore configStore;
	private Logger logger;
	
	public Logger getLogger(){ return logger; }
	public ConfigStore getConfigStore(){ return configStore; }
	
	private Application(){}
	
	public void init() throws Exception {
		
		initFolder();
		initLogger();
		initConfig();
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
	
	private void initConfig() throws Exception {
		
		File file = new File(getOutputFolder()+File.separator+ConfigStore.OUTPUT_FILE);
		if(file.isFile()){
			configStore = ConfigStore.load(file.getAbsolutePath());
		}else{
			configStore = new ConfigStore(file.getAbsolutePath());
			configStore.save();
		}
	}
	
	private void initFrame() {
		
		frame = new Frame();
		configStore.addListener(frame);
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){ shutdown(); }
        });
		frame.setVisible(true);
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
