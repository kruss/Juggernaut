package http;

import util.SystemTools;
import logger.Logger;
import logger.ILogConfig.Module;
import data.IOptionDelegate;

public class HttpTest implements IOptionDelegate {

	private Logger logger;
	
	public HttpTest(Logger logger) {
		this.logger = logger;
	}

	@Override
	public String getName(){ return "Test"; }

	@Override
	public void perform(String content) {
		
		try{
			String host = SystemTools.getHostName();
			int port = new Integer(content).intValue();
			String url = "http://"+host+":"+port;
			
			logger.log(Module.COMMON, "open: "+url);
			SystemTools.openBrowser(url);
			
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
	}
}
