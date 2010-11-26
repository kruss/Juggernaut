package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommandStreamer extends Task {
	
	private String name;
	private InputStream stream;
	private StringBuilder buffer;
	
	public CommandStreamer(String name, InputStream stream, Logger logger){
		super(logger);
		
		this.name = name;
		this.stream = stream;
		
		setName("Streamer("+name+")");
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
	        	observer.log(name+": "+line);
	        }
	        
			bufferedReader.close();
			streamReader.close();
		}catch(Exception e){ 
			observer.error(e); 
		}
	}
}
