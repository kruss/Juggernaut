package util;

import java.io.File;
import java.util.ArrayList;

import core.Constants;

public class CommandTask extends Task {

	private String command;
	private ArrayList<String> arguments;
	private String path;
	private Logger logger;

	private String output;
	private int result;
	
	public CommandTask(
			String command, ArrayList<String> arguments, 
			String path, Logger logger
	){
		super(logger);
		
		this.command = command;
		this.arguments = arguments;
		this.path = path;
		this.logger = logger;

		setName("Command("+command+")");
		output = "";
		result = Constants.PROCESS_NOK;
	}
	
	public boolean hasSucceded(){
		return result == Constants.PROCESS_OK;
	}
	
	public String getOutput(){ return output; }
	public int getResult(){ return result; }
	
	@Override
	protected void runTask() {
		
		try{
			String commandline = getCommandline(command, arguments);
			ProcessBuilder processBuilder = getProcessBuilder(commandline, path);
			
			logger.log("command: "+commandline);
			logger.log("path: "+path);
			
			Process process = processBuilder.start();
			CommandStreamer outputStream = new CommandStreamer("OUT", process.getInputStream(), logger);
			CommandStreamer errorStream = new CommandStreamer("ERR", process.getErrorStream(), logger);            
			outputStream.start();
			errorStream.start(); 
			
			try{
				process.waitFor();
				result = process.exitValue();
			}catch(InterruptedException e){ 
				process.destroy();	 // not destroying sub-processes on windows
			}
			
			while(errorStream.isAlive() || outputStream.isAlive()){ 
				SystemTools.sleep(50);
			}
			output = outputStream.getBuffer();
			
		}catch(Exception e){
			logger.error(e);
		}finally{
			logger.log("return: "+result);
		}
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
	
	private String getCommandline(String command, ArrayList<String> arguments) {
		return command+" "+StringTools.join(arguments, " ");
	}
}
