package core.runtime.logger;

public interface ILogConfig {

	public enum Module { COMMON, TASK, COMMAND, SMTP, HTTP }
	public enum Level { ERROR, NORMAL, DEBUG }

	/** get log-level for module */
	public Level getLogLevel(Module module);
}
