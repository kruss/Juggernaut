package util;

import java.io.File;

public interface ILoggingProvider {

	public void addListener(ILoggingListener listener);
	public void removeListener(ILoggingListener listener);
	public void clearListeners();
	public File getLogfile();
}
