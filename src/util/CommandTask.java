package util;

import java.io.File;


import logger.Logger;
import logger.ILogConfig.Module;


import core.Constants;
import core.TaskManager;

public class CommandTask extends Task {

	private String command;
	private String arguments;
	private String path;
	private TaskManager taskManager;
	private Logger logger;
	
	private StringBuilder output;
	private int result;
	
	public CommandTask(
			String command, 
			String arguments, 
			String path, 
			TaskManager taskManager, 
			Logger logger)
	{
		super("Command("+command+")", taskManager);
		this.command = command;
		this.arguments = arguments;
		this.path = path;
		this.taskManager = taskManager;
		this.logger = logger;
		
		this.output = new StringBuilder();
		result = Constants.PROCESS_NOK;
	}
	
	public boolean hasSucceded(){
		return result == Constants.PROCESS_OK;
	}
	
	public String getOutput(){ return output.toString(); }
	public int getResult(){ return result; }
	
	@Override
	protected void runTask() {
		
		try{
			String commandline = arguments.isEmpty() ? command : command+" "+arguments;
			ProcessBuilder processBuilder = getProcessBuilder(commandline, path);
			
			logger.log(Module.COMMAND, "command: "+commandline);
			logger.debug(Module.COMMAND, "directory: "+path);
			
			Process process = processBuilder.start();
			CommandStreamer outputStream = new CommandStreamer(
					this, "OUT", process.getInputStream(), taskManager, logger
			);
			CommandStreamer errorStream = new CommandStreamer(
					this, "ERR", process.getErrorStream(), taskManager, logger
			);            
			outputStream.start();
			errorStream.start(); 
			
			try{
				process.waitFor();
				result = process.exitValue();
			}catch(InterruptedException e){ 
				process.destroy();	 // not destroying sub-processes on windows
			}
			
			while(outputStream.isAlive() || errorStream.isAlive()){ 
				SystemTools.sleep(50);
			}
			
		}catch(Exception e){
			logger.error(Module.COMMAND, e);
		}finally{
			logger.debug(Module.COMMAND, "return: "+result);
		}
	}

	public synchronized void stream(String line) {
		output.append(line);
	}
	
	private ProcessBuilder getProcessBuilder(String commandline, String path) throws Exception {
		
		ProcessBuilder processBuilder = null;
		
		if(SystemTools.isWindowsOS()){
			processBuilder = new ProcessBuilder("cmd.exe", "/C", commandline);
		}else if(SystemTools.isLinuxOS()){
			processBuilder = new ProcessBuilder(commandline.split(" "));
		}else{
			throw new Exception("invalid os: "+SystemTools.getOSName());
		}
		
		File folder = new File(path);
		if(folder.isDirectory()){			
			processBuilder.directory(folder);
		}else{
			throw new Exception("invalid folder: "+path);
		}
		
		return processBuilder;
	}
}
