package logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import util.FileTools;
import util.StringTools;

import core.Application;
import core.Configuration;
import core.Constants;


public class Logger implements ILogProvider {

	public static final String OUTPUT_FILE = Constants.APP_NAME+".txt";
	public static final int BUFFER_MAX = 100;
	
	public enum Mode { FILE, CONSOLE, FILE_AND_CONSOLE }
	public enum Module { COMMON, COMMAND, TASK, HTTP }
	public enum Level { ERROR, NORMAL, DEBUG }

	protected Mode mode;
	private File logfile;
	private long logfileMax;
	private ArrayList<ILogListener> listeners;
	private ArrayList<String> buffer;
	
	/** set a logfile and the max-size in bytes (0 if unlimmited) */
	public void setLogfile(File logfile, long logfileMax){
		this.logfile = logfile;
		this.logfileMax = logfileMax;
		if(logfile.exists()){
			logfile.delete();
		}
	}
	public File getLogfile(){ return logfile; }
	
	public Logger(Mode mode){

		this.mode = mode;
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
	
	public void info(Module module, String text){ 
		createLog(module, Level.NORMAL, text, Type.INFO); 
	}
	public void emph(Module module, String text){ 	
		createLog(module, Level.NORMAL, text, Type.EMPHASISED); 				
	}
	public void log(Module module, String text){ 
		createLog(module, Level.NORMAL, text, Type.NORMAL); 
	}
    public void error(Module module, String text){ 
    	createLog(module, Level.ERROR, text, Type.ERROR); 
    }
    public void error(Module module, Exception e){ 
    	createLog(module, Level.ERROR, StringTools.trace(e), Type.ERROR); 
    }
    public void debug(Module module, String text){ 
    	createLog(module, Level.DEBUG, text, Type.DEBUG);
    }
    
    private synchronized void createLog(Module module, Level level, String text, Type type) {

    	if(isLogging(module, level)){
	    	String time = "["+StringTools.getTextDate(new Date())+"] ";
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
		
		Configuration config = Application.getInstance().getConfiguration();
		if(config != null){
			return getLevelValue(level) >= getLevelValue(config.getLogLevel(module));
		}else{
			return true;
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
    
	public static ArrayList<String> getLevelNames() {
		
		ArrayList<String> levels = new ArrayList<String>();
		for(Level level : Level.values()){
			levels.add(level.toString());
		}
		return levels;
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
