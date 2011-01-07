package http;

public interface IHttpServer {

	public IHttpConfig getConfig();
	
	public void startServer() throws Exception;
	public void stopServer() throws Exception;
	public boolean isRunning();
}
