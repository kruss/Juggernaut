package test;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import core.runtime.confluence.ConfluenceClient;
import core.runtime.confluence.IConfluenceClient;
import core.runtime.confluence.IConfluenceClient.PageInfo;
import core.runtime.logger.ILogConfig;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.logger.Logger.Mode;

public class ConfluenceClientTest {

	private static final String CONFLUENCE_SERVER = "http://10.42.84.232:8080";
	private static final String CONFLUENCE_SPACE_KEY = "ds";
	private static final String CONFLUENCE_PAGE_TITLE = "Juggernaut-Test";
	private static final String CONFLUENCE_PAGE_ID = "21037090";
	
	private Logger logger;
	private ConfluenceClient client;
	
	public ConfluenceClientTest(){
		logger = new Logger(null, Mode.CONSOLE);
		logger.setConfig(new ILogConfig(){
			@Override
			public Level getLogLevel(Module module) {
				return Level.DEBUG;
			}
		});
		client = new ConfluenceClient();
	}
	
	@Test public void testGetInfo() {
		
		logger.info(Module.COMMON, "Confluence Info");
		try{
			client.login(CONFLUENCE_SERVER, IConfluenceClient.RPC_USER, IConfluenceClient.RPC_USER, logger);
			try{
				PageInfo pageInfo = client.getInfo(CONFLUENCE_SPACE_KEY, CONFLUENCE_PAGE_TITLE, logger);
				assertEquals(CONFLUENCE_PAGE_ID, pageInfo.pageId);
			}finally{
				client.logout(logger);
			}
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
	
	@Test public void testUpdatePage() {
		
		logger.info(Module.COMMON, "Confluence Update");
		try{
			client.login(CONFLUENCE_SERVER, IConfluenceClient.RPC_USER, IConfluenceClient.RPC_USER, logger);
			try{
				PageInfo pageInfo = client.getInfo(CONFLUENCE_SPACE_KEY, CONFLUENCE_PAGE_TITLE, logger);
				client.updatePage(pageInfo, ""+(new Date()).getTime(), logger);
			}finally{
				client.logout(logger);
			}
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
}
