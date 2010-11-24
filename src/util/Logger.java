package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import core.Constants;


public class Logger implements ILoggingProvider {

	public static final String OUTPUT_FILE = Constants.APP_NAME+".log";
	
	public enum Level { DEBUG, NORMAL, EMPHASISED, ERROR, INFO }
	public enum Mode { FILE_ONLY, CONSOLE_ONLY, FILE_AND_CONSOLE }
	
	public static boolean VERBOSE = false;

	private Mode mode;
	private ArrayList<ILoggingListener> listeners;
	private File logfile;
	
	@Override
	public File getLogfile(){ return logfile; }
	
	public Logger(Mode mode){

		this.mode = mode;
		listeners = new ArrayList<ILoggingListener>();
		logfile = null;
	}
	
	@Override
	public void addListener(ILoggingListener listener){ 
		listeners.add(listener); 
		listener.setProvider(this);
	}
	
	@Override
	public void removeListener(ILoggingListener listener){ 
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
		for(ILoggingListener listener : listeners){
			listener.logged(log);
		}
	}
	
	public void setLogiFile(File logfile){
		
		this.logfile = logfile;
		if(logfile.exists()){
			logfile.delete();
		}
	}
	
	public synchronized void info(String text){ 	writeLog(text, Level.INFO); 					}
	public synchronized void emph(String text){ 	writeLog(text, Level.EMPHASISED); 				}
	public synchronized void log(String text){ 		writeLog(text, Level.NORMAL); 					}
    public synchronized void error(String text){ 	writeLog(text, Level.ERROR); 					}
    public synchronized void error(Exception e){ 	writeLog(StringTools.trace(e), Level.ERROR); 	}
    public synchronized void debug(String text){ 
    	if(VERBOSE){
    		writeLog(text, Level.DEBUG);					
    	}
    }
    
    private void writeLog(String text, Level level) {

    	String time = "["+StringTools.getTextDate(new Date())+"] ";
    	String log;
    	
		if(level == Level.INFO){
			log = "\n\t>>> "+text+" <<<\n\n";
		}else if(level == Level.EMPHASISED){
			log = time+text.toUpperCase()+"\n";
		}else if(level == Level.ERROR){
			log = time+"!!!\n"+text+"\n!!!\n";
		}else if(level == Level.DEBUG){
			log = time+"~> "+text+"\n";
		}else{
			log = time+text+"\n";
		}
		
		if(mode != Mode.CONSOLE_ONLY && logfile != null){
			writeFile(log);
		}
		if(mode != Mode.FILE_ONLY){
			writeSystem(log);
		}
		notifyListeners(log);
	}
    
    private void writeFile(String log){
    	
		try{
			FileTools.writeFile(logfile.getAbsolutePath(), log.replaceAll("\n", "\r\n"), true);
		}catch(Exception e){
			writeSystem(StringTools.trace(e));
		}
	}
    
    private void writeSystem(String log){
    	System.out.print(log);
    }
}
