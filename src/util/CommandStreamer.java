package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommandStreamer extends Task {

	private String name;
	private InputStream stream;
	private Logger logger;
	
	public CommandStreamer(String name, InputStream stream, Logger logger){
		
		this.name = name;
		this.stream = stream;
		this.logger = logger;
		
		setName("Stream("+name+")");
	}
	
	@Override
	protected void runTask() {
		
		try{
	    	InputStreamReader streamReader = new InputStreamReader(stream);
	    	BufferedReader bufferedReader = new BufferedReader(streamReader);
	    	
	        String line=null;
	        while( (line = bufferedReader.readLine()) != null){
	        	logger.log(name+": "+line);
	        }
	        
			bufferedReader.close();
			streamReader.close();
		}catch(Exception e){ 
			logger.error(e); 
		}
	}
}
