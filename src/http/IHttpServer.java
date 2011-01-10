package http;

import util.IChangeable;
import core.ISystemComponent;

public interface IHttpServer extends ISystemComponent, IChangeable {

	public IHttpConfig getConfig();
	
	public void startServer() throws Exception;
	public void stopServer() throws Exception;
	public boolean isRunning();
}
