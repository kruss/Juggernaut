package core.runtime.http;

import util.IChangeable;
import core.ISystemComponent;

public interface IHttpServer extends ISystemComponent, IChangeable {

	public IHttpConfig getConfig();
	
	public boolean isRunning();
	
	public void start() throws Exception;
	public void stop() throws Exception;
}
