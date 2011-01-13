package core.runtime.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;


import util.DateTools;
import util.FileTools;
import util.StringTools;

import core.Constants;
import core.runtime.logger.ILogConfig.Level;
import core.runtime.logger.ILogConfig.Module;


public class Logger implements ILogger, ILogProvider {

	public static final String OUTPUT_FILE = Constants.APP_NAME+".txt";
	public static final int BUFFER_MAX = 100;
	
	public enum Mode { FILE, CONSOLE, FILE_AND_CONSOLE }
	
	private Mode mode;
	private File logfile;
	private long logfileMax;
	private ArrayList<ILogListener> listeners;
	private ArrayList<String> buffer;
	private ILogConfig config;

	/** set a logfile and the max-size in bytes (0 if unlimmited) */
	public void setLogFile(File logfile, long logfileMax){
		
		this.logfile = logfile;
		this.logfileMax = logfileMax;
		if(logfile.exists()){
			logfile.delete();
		}else if(!logfile.getParentFile().exists()){
			logfile.getParentFile().mkdirs();
		}
	}
	public File getLogFile(){ return logfile; }
	
	public void setConfig(ILogConfig config){
		this.config = config;
	}
	
	public Logger(Mode mode){

		this.mode = mode;
		config = null;
		logfile = null;
		logfileMax = 0;
		listeners = new ArrayList<ILogListener>();
		buffer = new ArrayList<String>();
	}
	
	@Override
	public void addListener(ILogListener listener){ 
		listeners.add(listener); 
		listener.setProvider(this);
	}
	
	@Override
	public void removeListener(ILogListener listener){ 
		listeners.remove(listener); 
		listener.setProvider(null);
	}
	
	@Override
	public void clearListeners(){ 
		for(int i=listeners.size()-1; i>=0; i--){
			removeListener(listeners.get(i));
		}
	}
	
	public void notifyListeners(String log){
		for(ILogListener listener : listeners){
			listener.logged(log);
		}
	}
	
    @Override
    public String getBuffer(){ 
    	StringBuilder text = new StringBuilder();
    	for(String log : buffer){
    		text.append(log);
    	}
    	return text.toString();
    }
	
	private enum Type { DEBUG, NORMAL, EMPHASISED, ERROR, INFO }
	
	@Override
	public void info(Module module, String text){ 
		createLog(module, Level.NORMAL, text, Type.INFO); 
	}
	
	@Override
	public void emph(Module module, String text){ 	
		createLog(module, Level.NORMAL, text, Type.EMPHASISED); 				
	}
	
	@Override
	public void log(Module module, String text){ 
		createLog(module, Level.NORMAL, text, Type.NORMAL); 
	}
	
	@Override
    public void error(Module module, String text){ 
    	createLog(module, Level.ERROR, text, Type.ERROR); 
    }
    
    @Override
    public void error(Module module, Exception e){ 
    	createLog(module, Level.ERROR, StringTools.trace(e), Type.ERROR); 
    }
    
    @Override
    public void debug(Module module, String text){ 
    	createLog(module, Level.DEBUG, text, Type.DEBUG);
    }
    
    private synchronized void createLog(Module module, Level level, String text, Type type) {

    	if(isLogging(module, level)){
	    	String time = "["+DateTools.getTextDate(new Date())+"] ";
	    	String info = (module != Module.COMMON) ? "("+module.toString()+") " : "";
	    	String log;
	    	
			if(type == Type.INFO){
				log = "\n\t>>> "+text.toUpperCase()+" <<<\n\n";
			}else if(type == Type.EMPHASISED){
				log = time+info+text.toUpperCase()+"\n";
			}else if(type == Type.ERROR){
				log = time+info+"!!!\n"+text+"\n!!!\n";
			}else if(type == Type.DEBUG){
				log = time+info+"~> "+text+"\n";
			}else{
				log = time+info+text+"\n";
			}
			
			if(mode != Mode.CONSOLE && logfile != null){
				logToFile(log);
			}
			if(mode != Mode.FILE){
				logToConsole(log);
			}
			notifyListeners(log);
			buffer(log);
    	}
	}

	private boolean isLogging(Module module, Level level) {
		
		if(config != null){
			return getLevelValue(level) >= getLevelValue(config.getLogLevel(module));
		}else{
			return getLevelValue(level) >= getLevelValue(Level.NORMAL);
		}
	}
	private void logToFile(String log){
    	
		try{
			if(logfileMax > 0){
				if(logfile.length() >= logfileMax){
					FileTools.copyFile(
							logfile.getAbsolutePath(), 
							logfile.getAbsolutePath()+"."+(new Date()).getTime());
					logfile.delete();
				}
			}
			FileTools.writeFile(logfile.getAbsolutePath(), log.replaceAll("\n", "\r\n"), true);
		}catch(Exception e){
			logToConsole(StringTools.trace(e));
		}
	}
    
    private void logToConsole(String log){
    	System.out.print(log);
    }
    
    private void buffer(String log) {	
    	if(buffer.size() > BUFFER_MAX){
    		buffer.remove(0);
    	}
    	buffer.add(log);
    }
	
	public static int getLevelValue(Level level){
		
		if(level == Level.DEBUG){
			return 0;
		}else if(level == Level.NORMAL){
			return 1;
		}else if(level == Level.ERROR){
			return 2;
		}else{
			return -1;
		}
	}
}
