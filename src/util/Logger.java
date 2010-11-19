package util;

import java.io.File;
import java.util.Date;


public class Logger {

	public static final String OUTPUT_FILE = "logger.txt";
	
	private enum Level { VERBOSE, DEBUG, NORMAL, ERROR, INFO }
	
	private File logfile;
	private boolean verbose;
	
	public File getLogfile(){ return logfile; }
	public void setVerbose(boolean verbose){ this.verbose = verbose; };
	
	public Logger(File logfile){

		this.logfile = logfile;
		this.verbose = false;
		
		if(logfile.exists()){
			logfile.delete();
		}
	}
	
	public synchronized void info(String text){ 		writeLog(text, Level.INFO); 					}
	public synchronized void log(String text){ 			writeLog(text, Level.NORMAL); 					}
    public synchronized void error(String text){ 		writeLog(text, Level.ERROR); 					}
    public synchronized void error(Exception e){ 		writeLog(StringTools.trace(e), Level.ERROR); 	}
    public synchronized void debug(String text){ 		writeLog(text, Level.DEBUG);					}
    
    private void writeLog(String text, Level level) {

    	String time = "["+StringTools.getTextDate(new Date())+"]";
    	String log;
    	
		if(level == Level.INFO){
			log = "\n\t>>> "+text+" <<<\n\n";
		}else if(level == Level.ERROR){
			log = time+" "+"!!!\n"+text+"\n!!!\n";
		}else if(level == Level.DEBUG){
			log = time+" ~> "+text+"\n";
		}else{
			log = time+" "+text+"\n";
		}
		
		writeFile(log);
    	if(level != Level.DEBUG || verbose){
    		writeSystem(log);
    	}
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
