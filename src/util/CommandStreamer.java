package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import logger.Logger;
import logger.Logger.Module;


public class CommandStreamer extends Task {
	
	private CommandTask parent;
	private InputStream stream;
	private String name;
	
	public String getStreamName(){ return name; }
	
	public CommandStreamer(CommandTask parent, String name, InputStream stream, Logger logger){
		
		super("Streamer("+name+")", logger);
		this.parent = parent;
		this.stream = stream;
		this.name = name;
	}
	
	@Override
	protected void runTask() {
		
		try{
	    	InputStreamReader streamReader = new InputStreamReader(stream);
	    	BufferedReader bufferedReader = new BufferedReader(streamReader);
	    	
	        String line=null;
	        while( (line = bufferedReader.readLine()) != null){
	        	parent.stream(line+"\n");
	        	observer.debug(Module.COMMAND, line);
	        }
	        
			bufferedReader.close();
			streamReader.close();
		}catch(Exception e){ 
			observer.error(Module.COMMAND, e); 
		}
	}
}
