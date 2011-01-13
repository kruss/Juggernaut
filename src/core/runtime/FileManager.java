package core.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import core.Constants;
import core.ISystemComponent;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;



import util.CommandTask;
import util.FileTools;
import util.SystemTools;

/** 
 * The file-manager controls file-system resources
 */
public class FileManager implements ISystemComponent {
	
	private TaskManager taskManager;
	
	private IToolConfig config;

	public FileManager(TaskManager taskManager){
		
		this.taskManager = taskManager;
		config = null;
	}
	
	public void setConfig(IToolConfig fileConfig){ this.config = fileConfig; }
	
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
		
		if(getTempFolder().isDirectory()){
			FileTools.deleteFolder(getTempFolderPath());
		}
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
	public String getLaunchHistoryFolderPath(Date date) {
		return getHistoryFolderPath()+File.separator+date.getTime();
	}
	public File getLaunchHistoryFolder(Date date){
		return new File(getLaunchHistoryFolderPath(date));
	}
	
	public boolean hasUnlocker() {
		return config != null && !config.getUnlocker().isEmpty();
	}
	
	public void deleteWithUnlocker(File file, Logger logger) throws Exception {
		
		if(hasUnlocker()){
			String command = config.getUnlocker();
			String arguments = file.getAbsolutePath();
			String path = file.getParentFile().getAbsolutePath();
			CommandTask task = new CommandTask(
					command, arguments, path, taskManager, logger
			);
			task.syncRun(0, IToolConfig.UNLOCKER_TIMEOUT);
			logger.debug(Module.COMMON, "delete: "+file.getAbsolutePath());
			if(file.isFile()){
				FileTools.deleteFile(file.getAbsolutePath());
			}else if(file.isDirectory()){
				FileTools.deleteFolder(file.getAbsolutePath());
			}
		}
	}
}
