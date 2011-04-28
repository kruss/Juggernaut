package core.runtime.confluence;

import core.runtime.logger.Logger;

public interface IConfluenceClient {
	
	public static String RPC_USER  = "xmlrpc";
	
	public void login(String url, String user, String password, Logger logger) throws Exception;
	public void logout(Logger logger) throws Exception;
	
	public PageInfo getInfo(String spaceKey, String pageTitle, Logger logger) throws Exception;
	
	public class PageInfo {
		public String spaceKey;
		public String pageId;
		public String pageVersion;
		public String pageTitle;
	}
	
	public void updatePage(PageInfo pageInfo, String pageContent, Logger logger) throws Exception;
}
