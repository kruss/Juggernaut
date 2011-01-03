package logger;

import logger.ILogConfig.Module;

public interface ILogger {
	
	/** create a log-segment */
	public void info(Module module, String text);
	
	/** create an emphasised log */
	public void emph(Module module, String text);
	
	/** create a normal log */
	public void log(Module module, String text);
	
	/** create an error log */
    public void error(Module module, String text);
    
    /** create an error log */
    public void error(Module module, Exception e);
    
    /** create a debug log */
    public void debug(Module module, String text);
}

