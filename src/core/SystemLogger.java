package core;

import java.io.File;

import util.SystemTools;


import logger.Logger;
import logger.ILogConfig.Module;

public class SystemLogger extends Logger implements ISystemComponent {
	
	public SystemLogger() {
		super(Mode.FILE_AND_CONSOLE);
	}

	@Override
	public void init() throws Exception {
		initLogger();
	}
	
	@Override
	public void shutdown() throws Exception {
		cleanupLogfiles();
	}

	private void initLogger() {
		File logFile = new File(SystemTools.getWorkingDir()+File.separator+Logger.OUTPUT_FILE);
		setLogFile(logFile, Constants.LOGFILE_MAX);
		info(Module.COMMON, Constants.APP_FULL_NAME);
	}

	private void cleanupLogfiles() throws Exception {
		for(File file : new File(SystemTools.getWorkingDir()).listFiles()){
			if(file.isFile() && file.getName().startsWith(Logger.OUTPUT_FILE)){
				file.delete();
			}
		}
	}
}
