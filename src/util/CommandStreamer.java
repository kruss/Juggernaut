package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import logger.Logger;
import logger.Logger.Module;


public class CommandStreamer extends Task {
	
	private InputStream stream;
	private StringBuilder buffer;
	
	public CommandStreamer(String name, InputStream stream, Logger logger){
		
		super("Streamer("+name+")", logger);
		this.stream = stream;
		buffer = new StringBuilder();
	}
	
	public String getBuffer() { return buffer.toString(); }
	
	@Override
	protected void runTask() {
		
		try{
	    	InputStreamReader streamReader = new InputStreamReader(stream);
	    	BufferedReader bufferedReader = new BufferedReader(streamReader);
	    	
	        String line=null;
	        while( (line = bufferedReader.readLine()) != null){
	        	buffer.append(line+"\n"); // unix-style for regex-processing
	        	observer.log(Module.CMD, line);
	        }
	        
			bufferedReader.close();
			streamReader.close();
		}catch(Exception e){ 
			observer.error(Module.CMD, e); 
		}
	}
}
