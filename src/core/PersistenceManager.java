package core;

import java.io.File;
import java.util.ArrayList;

import util.FileTools;
import util.Logger;

import data.LaunchConfig;

// TODO temp draft
public class PersistenceManager {
	
	public static void initialize(ArrayList<File> folders) {
		
		for(File folder : folders){			
			if(!folder.isDirectory()){
				folder.mkdirs();
			}
		}
	}

	/** cleanup folder for legacy items not containing a launch id within name */
	public static void cleanup(Configuration configuration, File folder, Logger logger) throws Exception {
		
		for(File file : folder.listFiles()){
			if(isLegacy(configuration, file)){
				delete(file, logger);
			}
		}
	}

	private static boolean isLegacy(Configuration configuration, File file) {
		
		for(LaunchConfig config : configuration.getLaunchConfigs()){
			if(file.getName().contains(config.getId())){
				return false;
			}
		}
		return true;
	}
	
	public static void delete(File file, Logger logger) throws Exception {
		
		// TODO retry with unlocker for windows
		logger.debug("delete: "+file.getAbsolutePath());
		if(file.isFile()){
			FileTools.deleteFile(file.getAbsolutePath());
		}else if(file.isDirectory()){
			FileTools.deleteFolder(file.getAbsolutePath());
		}
	}
}
