package core;

import java.io.File;
import java.util.ArrayList;

import logger.Logger;
import logger.Logger.Module;

import util.FileTools;
import util.SystemTools;

import data.LaunchConfig;

/** 
 * The file-manager controls file-system resources
 */
public class FileManager {
	
	private Application application;
	
	public FileManager(){
		application = Application.getInstance();
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
	
	public void init() throws Exception {
		initFolders();
	}
	
	public void shutdown() throws Exception {
		cleanBuildFolder();
		deleteTempFolder();
		deleteOldLogfiles();
	}

	private void initFolders() throws Exception {
		
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

	private void cleanBuildFolder() throws Exception {
		
		for(File file : getBuildFolder().listFiles()){
			if(isLegacyBuildFile(file)){
				delete(file);
			}
		}
	}

	private boolean isLegacyBuildFile(File folder) {
		
		for(LaunchConfig config : application.getConfig().getLaunchConfigs()){
			if(folder.getName().contains(config.getId())){
				return false;
			}
		}
		return true;
	}
	
	private void deleteTempFolder() throws Exception {
		delete(getTempFolder());
	}
	
	private void deleteOldLogfiles() throws Exception {
		for(File file : getDataFolder().listFiles()){
			if(file.getName().startsWith(Logger.OUTPUT_FILE+".")){
				delete(file);
			}
		}
	}
	
	private void delete(File file) throws Exception {
		
		// TODO retry with unlocker for windows
		application.getLogger().debug(Module.APP, "delete: "+file.getAbsolutePath());
		if(file.isFile()){
			FileTools.deleteFile(file.getAbsolutePath());
		}else if(file.isDirectory()){
			FileTools.deleteFolder(file.getAbsolutePath());
		}
	}
}
