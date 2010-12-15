package util;

import java.io.File;

import logger.Logger;
import logger.Logger.Module;


import core.Constants;

public class CommandTask extends Task {

	private String command;
	private String arguments;
	private String path;
	private Logger logger;
	
	private StringBuilder output;
	private int result;
	
	public CommandTask(
			String command, String arguments, 
			String path, Logger logger
	){
		super("Command("+command+")", logger);
		this.command = command;
		this.arguments = arguments;
		this.path = path;
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
					this, "OUT", process.getInputStream(), logger
			);
			CommandStreamer errorStream = new CommandStreamer(
					this, "ERR", process.getErrorStream(), logger
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
