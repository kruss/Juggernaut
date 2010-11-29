package operation;

import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;
import repository.IRepositoryClient.Revision;

import lifecycle.LaunchAgent;
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
		
		parent.getPropertyManager().addProperty(config.getId(), Property.REVISION.toString(), revision);
	}

	@Override
	protected void execute() throws Exception {
		
		String url = parent.getPropertyManager().expand(config.getUrl());
		CheckoutInfo result = client.checkout(url, Revision.HEAD.toString(), parent.getFolder());
		
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
