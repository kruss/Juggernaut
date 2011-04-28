package core.runtime.confluence;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import core.ISystemComponent;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class ConfluenceClient implements ISystemComponent, IConfluenceClient {

	private static String RPC_PATH  = "/rpc/xmlrpc";
	
	private XmlRpcClient rpc;
	private String login;
	
	public ConfluenceClient(){
		rpc = null;
		login = null;
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	@Override
	public void login(String url, String user, String password, Logger logger) throws Exception {
		
		if(login == null){
			logger.log(Module.COMMON, "login to confluence ("+user+"@"+url+")");
			
			String uri = url+RPC_PATH;
			XmlRpcClientConfigImpl rpcConfig = new XmlRpcClientConfigImpl();
			rpcConfig.setServerURL(new URL(uri));
	        rpc = new XmlRpcClient();
			rpc.setConfig(rpcConfig);
			
	        Vector<String> params = new Vector<String>(2);
	        params.add(user);
	        params.add(password);
	        login = (String) rpc.execute("confluence1.login", params);
	        
	        logger.debug(Module.COMMON, "confluence login: "+login);
		}else{
			throw new Exception("already logged in");
		}
	}

	@Override
	public void logout(Logger logger) throws Exception {
		
		if(login != null){
			logger.log(Module.COMMON, "logout from confluence");
			try{
		    	Vector<String> params = new Vector<String>(1);
		    	params.add(login);
		    	Boolean result = (Boolean) rpc.execute("confluence1.logout", params);
				
		    	logger.debug(Module.COMMON, "confluence logout: "+result);
			}finally{
				rpc = null;
				login = null;
			}
		}else{
			throw new Exception("not logged in");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PageInfo getInfo(String spaceKey, String pageTitle, Logger logger) throws Exception {
		
		if(login != null){
			logger.debug(Module.COMMON, "get confluence page-id ("+spaceKey+"::"+pageTitle+")");
			
	        Map<String,String> result = (Map<String,String>)rpc.execute(
	        	"confluence1.getPage",
	            new Object[] {login, spaceKey, pageTitle}
	        );
	        
	        PageInfo pageInfo = new PageInfo();
	        pageInfo.spaceKey = spaceKey;
	        pageInfo.pageId = result.get("id");
	        pageInfo.pageVersion = result.get("version");
	        pageInfo.pageTitle = pageTitle;
	        
	        logger.debug(Module.COMMON, "confluence page-id: "+pageInfo.pageId+" ("+pageInfo.pageVersion+")");
		    return pageInfo;	
		}else{
			throw new Exception("not logged in");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updatePage(PageInfo pageInfo, String pageContent, Logger logger) throws Exception {
		
		if(login != null){
			logger.debug(Module.COMMON, "update content for confluence page-id ("+pageInfo.pageId+")");
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("space", pageInfo.spaceKey);
			params.put("id", pageInfo.pageId);
			params.put("title", pageInfo.pageTitle);
			params.put("version", pageInfo.pageVersion);
			params.put("content", pageContent);
			
	        Map<String,String> result = (Map<String,String>)rpc.execute(
	        	"confluence1.storePage",
	            new Object[] {login, params}
	        );   
	        
	        String update = result.get("content");
	        if(update == null || !update.equals(pageContent)){
	        	throw new Exception("update failed");
	        }else{
	        	logger.debug(Module.COMMON, "confluence updated ("+update.getBytes().length+" bytes)");
	        }
		}else{
			throw new Exception("not logged in");
		}
	}
}
