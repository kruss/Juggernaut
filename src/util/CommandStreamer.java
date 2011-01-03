package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import core.TaskManager;


import logger.Logger;
import logger.ILogConfig.Module;


public class CommandStreamer extends Task {
	
	private CommandTask parent;
	private InputStream stream;
	private String name;
	private Logger logger;
	
	public String getStreamName(){ return name; }
	
	public CommandStreamer(CommandTask parent, String name, InputStream stream, TaskManager taskManager, Logger logger){
		
		super("Streamer("+name+")", taskManager);
		this.parent = parent;
		this.stream = stream;
		this.name = name;
		this.logger = logger;
	}
	
	@Override
	protected void runTask() {
		
		try{
	    	InputStreamReader streamReader = new InputStreamReader(stream);
	    	BufferedReader bufferedReader = new BufferedReader(streamReader);
	    	
	        String line=null;
	        while( (line = bufferedReader.readLine()) != null){
	        	parent.stream(line+"\n");
	        	logger.debug(Module.COMMAND, line);
	        }
	        
			bufferedReader.close();
			streamReader.close();
		}catch(Exception e){ 
			logger.error(Module.COMMAND, e); 
		}
	}
}
