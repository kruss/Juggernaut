package logger;


public interface ILogProvider {

	public void addListener(ILogListener listener);
	public void removeListener(ILogListener listener);
	public void clearListeners();
	public String getBuffer();
}
