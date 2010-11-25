package util;

import java.io.File;
import java.util.ArrayList;

import core.Configuration;
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
				logger.log("cleanup: "+file.getAbsolutePath());
				if(file.isFile()){
					FileTools.deleteFile(file.getAbsolutePath());
				}else if(file.isDirectory()){
					FileTools.deleteFolder(file.getAbsolutePath());
				}
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
}
