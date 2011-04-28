package core.runtime.confluence;

import core.launch.data.StatusManager.Status;
import core.runtime.logger.Logger;
import ui.dialog.AbstractUITest;

public class ConfluenceTest extends AbstractUITest {

	private IConfluenceClient client;
	
	public ConfluenceTest(ConfluenceClient client, Logger logger) {
		super(logger);
		this.client = client;
	}

	@Override
	protected TestStatus performTest(String url) throws Exception {
		
		if(!url.isEmpty()){
			
			try{
				try{
					client.login(url, IConfluenceClient.RPC_USER, IConfluenceClient.RPC_USER, logger);
				}finally{
					client.logout(logger);
				}
				return new TestStatus(Status.SUCCEED, "Logged as "+IConfluenceClient.RPC_USER+" in at "+url);
			}catch(Exception e){
				return new TestStatus(e);
			}
		}else{
			return new TestStatus(Status.ERROR, "No confluence-server specified");
		}
	}
}
