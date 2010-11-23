package util;

public interface ILoggingProvider {

	public void addListener(ILoggingListener listener);
	public void removeListener(ILoggingListener listener);
	public void clearListeners();
}
