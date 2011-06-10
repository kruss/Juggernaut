package util;

import java.io.File;
import java.util.HashMap;



import core.Constants;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class CommandTask extends Task {

	private String command;
	private String arguments;
	private String path;
	private HashMap<String, String> environment;
	private TaskManager taskManager;
	private Logger logger;
	
	private StringBuilder output;
	private int result;
	
	public CommandTask(
			String command, 
			String arguments, 
			String path, 
			HashMap<String, String> environment,
			TaskManager taskManager, 
			Logger logger)
	{
		super("Command("+command+")", taskManager);
		this.command = command;
		this.arguments = arguments;
		this.path = path;
		this.environment = environment;
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
			ProcessBuilder processBuilder = getProcessBuilder(commandline, path, environment);
			
			logger.log(Module.COMMAND, "command: "+commandline);
			logger.debug(Module.COMMAND, "directory: "+path);
			
			Process process = processBuilder.start();
			CommandStreamer outputStream = new CommandStreamer(
					this, "OUT", process.getInputStream(), taskManager, logger
			);
			CommandStreamer errorStream = new CommandStreamer(
					this, "ERR", process.getErrorStream(), taskManager, logger
			);    
			
			outputStream.asyncRun(0, timeout);
			errorStream.asyncRun(0, timeout);
			try{
				process.waitFor();
				result = process.exitValue();
				waitForStreams(outputStream, errorStream);
			}catch(InterruptedException e){ 
				process.destroy();	 // not destroying sub-processes on windows
			}finally{
				outputStream.syncStop(1000);
				errorStream.syncStop(1000);
			}
			
		}catch(Exception e){
			logger.error(Module.COMMAND, e);
		}finally{
			logger.debug(Module.COMMAND, "return: "+result);
		}
	}

	private void waitForStreams(CommandStreamer outputStream, CommandStreamer errorStream) throws Exception {
		while(true){
			if(outputStream.getState() == State.RUNNING || errorStream.getState() == State.RUNNING){
				Thread.sleep(100);
			}else{
				return;
			}
		}
	}

	public synchronized void stream(String line) {
		output.append(line);
	}
	
	private ProcessBuilder getProcessBuilder(
			String commandline, 
			String path, 
			HashMap<String, String> environment
	) throws Exception {
		
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
		
		if(environment != null){
			processBuilder.environment().putAll(environment);
		}
		
		return processBuilder;
	}
}
