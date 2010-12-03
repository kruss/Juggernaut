package operation;

import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;

import lifecycle.LaunchAgent;
import lifecycle.PropertyContainer;
import lifecycle.StatusManager.Status;
import data.AbstractOperation;

public class SVNOperation extends AbstractOperation {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNOperationConfig config;
	
	public SVNOperation(LaunchAgent parent, SVNOperationConfig config) {
		super(parent, config);
		this.config = config;
		client = new SVNClient(parent.getLogger());
	}
	
	private void setCurrentRevision(String revision){
		
		parent.getPropertyContainer().addProperty(config.getId(), Property.REVISION.toString(), revision);
	}

	@Override
	protected void execute() throws Exception {
		
		String url = PropertyContainer.expand(parent.getPropertyContainer(), config.getUrl());
		String revision = PropertyContainer.expand(parent.getPropertyContainer(), config.getRevision());
		CheckoutInfo result = client.checkout(url, revision, parent.getFolder());
		
		if(result.revision != null){
			setCurrentRevision(result.revision);
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		
		// TODO get commits of revision intervall
		// TODO provide the svn command-output as artifact
	}
}
