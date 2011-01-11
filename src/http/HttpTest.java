package http;

import ui.AbstractUITest;
import util.SystemTools;
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
	protected String performTest(String content) throws Exception {
	
		if(config.isHttpServer()){
			
			String host = SystemTools.getHostName();
			int port = new Integer(content).intValue();
			String url = "http://"+host+":"+port;
			
			SystemTools.openBrowser(url);
			return "Browser: "+url;
		}else{
			throw CANCEL;
		}
	}
}
