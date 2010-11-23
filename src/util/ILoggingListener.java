package util;

public interface ILoggingListener {
	
	public void setProvider(ILoggingProvider provider);
	public ILoggingProvider getProvider();

	public void deregister();
	
	public void logged(String log);
}
