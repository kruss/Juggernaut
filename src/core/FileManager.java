package core;

import java.io.File;
import java.util.ArrayList;


import logger.Logger;
import logger.ILogConfig.Module;

import util.FileTools;
import util.SystemTools;

/** 
 * The file-manager controls file-system resources
 */
public class FileManager implements ISystemComponent {
	
	private Logger logger;
	
	public FileManager(Logger logger){
		this.logger = logger;
	}
	
	@Override
	public void init() throws Exception {

		ArrayList<File> folders = new ArrayList<File>();
		folders.add(getDataFolder());
		folders.add(getBuildFolder());
		folders.add(getHistoryFolder());
		folders.add(getTempFolder());
		for(File folder : folders){			
			if(!folder.isDirectory()){
				if(!folder.mkdirs()){
					throw new Exception("unable to create: "+folder.getAbsolutePath());
				}
			}
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		delete(getTempFolder());
	}
	
	public String getDataFolderPath(){
		return SystemTools.getWorkingDir()+File.separator+Constants.DATA_FOLDER;
	}
	public File getDataFolder(){
		return new File(getDataFolderPath());
	}
	public String getBuildFolderPath(){
		return SystemTools.getWorkingDir()+File.separator+Constants.BUILD_FOLDER;
	}
	public File getBuildFolder(){
		return new File(getBuildFolderPath());
	}
	public String getHistoryFolderPath(){
		return SystemTools.getWorkingDir()+File.separator+Constants.HISTORY_FOLDER;
	}
	public File getHistoryFolder(){
		return new File(getHistoryFolderPath());
	}
	public String getTempFolderPath(){
		return SystemTools.getWorkingDir()+File.separator+Constants.TEMP_FOLDER;
	}
	public File getTempFolder(){
		return new File(getTempFolderPath());
	}
	
	public String getLaunchFolderPath(String id) {
		return getBuildFolderPath()+File.separator+id;
	}
	public File getLaunchFolder(String id){
		return new File(getLaunchFolderPath(id));
	}
	
	// TODO retry with unlocker for windows
	public void delete(File file) throws Exception {
		
		logger.debug(Module.COMMON, "delete: "+file.getAbsolutePath());
		if(file.isFile()){
			FileTools.deleteFile(file.getAbsolutePath());
		}else if(file.isDirectory()){
			FileTools.deleteFolder(file.getAbsolutePath());
		}
	}
}
