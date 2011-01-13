package core.runtime.logger;

import java.io.File;

import core.Constants;
import core.ISystemComponent;
import core.runtime.logger.ILogConfig.Module;

import util.SystemTools;

public class SystemLogger extends Logger implements ISystemComponent {
	
	public SystemLogger() {
		super(Mode.FILE_AND_CONSOLE);
	}

	@Override
	public void init() throws Exception {
		createLogFile();
		info(Module.COMMON, Constants.APP_FULL_NAME);
	}
	
	@Override
	public void shutdown() throws Exception {
		cleanup();
	}

	private void createLogFile() {
		
		File logFile = new File(SystemTools.getWorkingDir()+File.separator+Logger.OUTPUT_FILE);
		setLogFile(logFile, Constants.LOGFILE_MAX);
	}

	private void cleanup() throws Exception {
		for(File file : new File(SystemTools.getWorkingDir()).listFiles()){
			if(file.isFile() && file.getName().startsWith(Logger.OUTPUT_FILE)){
				file.delete();
			}
		}
	}
}
