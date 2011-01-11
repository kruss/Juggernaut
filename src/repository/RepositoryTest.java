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
	protected String performTest(String content) throws Exception {
		
		String url = content;
		if(url.isEmpty()){
			throw new Exception("Missing URL");
		}
		
		RevisionInfo info = client.getInfo(url);
		status = Status.SUCCEED;
		return info.toString();
	}
}
