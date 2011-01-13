package core.launch.repository;

import core.launch.data.StatusManager.Status;
import core.launch.repository.IRepositoryClient.RevisionInfo;
import core.runtime.logger.Logger;
import ui.dialog.AbstractUITest;

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
