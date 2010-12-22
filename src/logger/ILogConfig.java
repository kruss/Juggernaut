package logger;

import logger.Logger.Level;
import logger.Logger.Module;

public interface ILogConfig {

	/** get log-level for module */
	public Level getLogLevel(Module module);
}
