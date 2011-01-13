package core.runtime.logger;


public interface ILogListener {
	
	public void setProvider(ILogProvider provider);
	public ILogProvider getProvider();

	public void deregister();
	
	public void logged(String log);
}
