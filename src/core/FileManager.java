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
public class FileManager implements ISystemComponent {
	
	private Application application;
	
	public FileManager(Application application){
		this.application = application;
	}
	
	@Override
	public void init() throws Exception {
		initFolders();
	}
	
	@Override
	public void shutdown() throws Exception {
		cleanBuildFolder();
		deleteTempFolder();
		deleteOldLogfiles();
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
		
		Configuration configuration = application.getConfiguration();
		if(configuration != null){
			for(File file : getBuildFolder().listFiles()){
				if(isLegacyBuildFile(configuration, file)){
					delete(file);
				}
			}
		}
	}

	private boolean isLegacyBuildFile(Configuration configuration, File file) {

		for(LaunchConfig config : configuration.getLaunchConfigs()){
			if(file.getName().contains(config.getId())){
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
	
	// TODO retry with unlocker for windows
	private void delete(File file) throws Exception {
		
		if(application.getLogger() != null){
			application.getLogger().debug(Module.COMMON, "delete: "+file.getAbsolutePath());
		}
		if(file.isFile()){
			FileTools.deleteFile(file.getAbsolutePath());
		}else if(file.isDirectory()){
			FileTools.deleteFolder(file.getAbsolutePath());
		}
	}
}
