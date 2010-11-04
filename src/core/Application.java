package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
		
		Application.getInstance();
	}
	
	private Configuration configuration;
	private Frame frame;
	private Logger logger;
	
	public Logger getLogger(){ return logger; }
	
	private Application(){
		
		try{ 
			init(); 
			logger.info(Constants.APP_NAME+" ("+Constants.APP_VERSION+")");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(Constants.PROCESS_NOK);
		}
	}
	
	private void init() throws Exception {
		
		initFolder();
		initLogger();
		initConfiguration();
		initFrame();
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
	
	private void initConfiguration() throws Exception {
		
		File file = new File(getOutputFolder()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			configuration = Configuration.load(file.getAbsolutePath());
		}else{
			configuration = new Configuration(file.getAbsolutePath());
			configuration.save();
		}
		
	}
	
	private void initFrame() {
		
		frame = new Frame();
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	frame.setStatus("Close");
            	shutdown();
            }
        });
		frame.setVisible(true);
	}
	
	public void shutdown(){
		
		logger.info("Shutdown");
		try{
			configuration.chekForSave();
		}catch(Exception e){
			logger.error(e);
			UiTools.errorDialog(e);
		}
		System.exit(Constants.PROCESS_OK);
	}
	
	
	private String getOutputFolder(){
		return FileTools.getWorkingDir()+File.separator+Constants.OUTPUT_FOLDER;
	}
}
