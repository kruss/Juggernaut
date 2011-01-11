package repository;

import repository.IRepositoryClient.RevisionInfo;
import launch.StatusManager.Status;
import logger.Logger;
import ui.AbstractUITest;

public class RepositoryTest extends AbstractUITest {

	private IRepositoryClient client;
	
	public RepositoryTest(IRepositoryClient client, Logger logger){
		super(logger);
		this.client = client;
	}

	@Override
	protected TestStatus performTest(String url) throws Exception {
		
		if(!url.isEmpty()){			
			RevisionInfo info = client.getInfo(url);
			return new TestStatus(Status.SUCCEED, info.toString());
		}else{
			return new TestStatus(Status.ERROR, "Missing URL");
		}
	}
}
