package core.runtime.confluence;

public interface IConfluenceClient {
	
	public void login(String url) throws Exception;
	public void logout() throws Exception;
	
	public PageInfo getInfo(String spaceKey, String pageTitle) throws Exception;
	
	public class PageInfo {
		public String spaceKey;
		public String pageId;
		public String pageVersion;
		public String pageTitle;
	}
	
	public void updatePage(PageInfo pageInfo, String pageContent) throws Exception;
}
