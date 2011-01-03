package logger;

public interface ILogConfig {

	public enum Module { COMMON, COMMAND, TASK, HTTP, SMTP }
	public enum Level { ERROR, NORMAL, DEBUG }

	/** get log-level for module */
	public Level getLogLevel(Module module);
}
