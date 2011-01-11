package http;

import ui.AbstractUITest;
import util.SystemTools;
import launch.StatusManager.Status;
import logger.Logger;

public class HttpTest extends AbstractUITest {
	
	private IHttpConfig config;
	
	public HttpTest(IHttpConfig config, Logger logger) {
		super(logger);
		this.config = config;
	}

	@Override
	public String getName(){ return "Test"; }
	
	@Override
	protected TestStatus performTest(String port) throws Exception {
	
		if(config.isHttpServer()){
			
			String host = SystemTools.getHostName();
			String url = "http://"+host+":"+port;
			
			SystemTools.openBrowser(url);		
			return new TestStatus(Status.SUCCEED, "Browser: "+url);
		}else{
			return new TestStatus(Status.ERROR, "HTTP OFF");
		}
	}
}
