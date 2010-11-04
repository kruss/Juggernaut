package core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import ui.Frame;
import util.FileTools;
import logger.Logger;

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
	
	private Frame frame;
	private Logger logger;
	
	public Logger getLogger(){ return logger; }
	
	private Application(){
		
		init();
		logger.info(Constants.APP_NAME+" ("+Constants.APP_VERSION+")");
	}
	
	private void init(){
		
		initFolders();
		initLogger();
		initFrame();
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

	private void initFolders() {

		ArrayList<File> folders = new ArrayList<File>();
		folders.add(new File(getFolderPath(Constants.CONFIG_FOLDER)));
		folders.add(new File(getFolderPath(Constants.DATA_FOLDER)));
		for(File folder : folders){
			if(!folder.isDirectory()){
				folder.mkdirs();
			}
		}
	}
	
	private String getFolderPath(String name){
		return FileTools.getWorkingDir()+File.separator+name;
	}

	private void initLogger() {
		
		logger = new Logger(
				new File(FileTools.getWorkingDir()+File.separator+Constants.APP_NAME+".log")
		);
	}
	
	public void shutdown(){
		
		logger.info("Shutdown");
		
		System.exit(Constants.PROCESS_OK);
	}
}
