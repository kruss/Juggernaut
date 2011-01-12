package operation;

import core.Cache;
import core.Configuration;
import core.TaskManager;
import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;
import repository.IRepositoryClient.HistoryInfo;

import launch.LaunchAgent;
import launch.StatusManager.Status;
import launch.Property;

import logger.ILogConfig.Module;
import data.AbstractOperation;
import data.Artifact;

public class SVNOperation extends AbstractOperation implements IRepositoryOperation {

	private enum PROPERTY { URL, REVISION };
	
	private SVNClient client;
	private SVNOperationConfig config;
	
	public String url;
	public String lastRevision;
	public String currentRevision;
	public HistoryInfo history;		
	
	@Override
	public String getUrl(){ return url; }
	@Override
	public String getRevision(){ return currentRevision; }
	@Override
	public HistoryInfo getHistory(){ return history; }
	
	public SVNOperation(Configuration configuration, Cache cache, TaskManager taskManager, LaunchAgent parent, SVNOperationConfig config) {
		super(configuration, cache, taskManager, parent, config);
		
		this.config = config;
		client = new SVNClient(taskManager,parent.getLogger());
		
		url = null;
		lastRevision = null;
		currentRevision = null;
		history = null;
	}
	
	@Override
	public String getDescription() {
		return getUrlProperty() + (currentRevision != null ? " ("+currentRevision+")" : "");
	}
	
	private void setLastRevisionProperty(String revision){
		cache.setProperty(
				config.getId(), PROPERTY.REVISION.toString(), revision
		);
	}
	
	private String getLastRevisionProperty(){
		return cache.getProperty(
				config.getId(), PROPERTY.REVISION.toString()
		);
	}

	private String getUrlProperty() {
		return parent.getPropertyContainer().expand(config.getUrl());
	}
	
	private String getRevisionProperty() {
		return parent.getPropertyContainer().expand(config.getRevision());
	}
	
	private void setRevisionProperty(String revision) {
		parent.getPropertyContainer().setProperty(
				new Property(config.getId(), PROPERTY.REVISION.toString(), revision)
		);
	}
	
	@Override
	protected void execute() throws Exception {
		
		url = getUrlProperty();
		String revision = getRevisionProperty();
		svnCheckout(url, revision);
		
		try{
			svnHistory();
		}catch(Exception e){
			logger.error(Module.COMMAND, e);
		}
		
		statusManager.setStatus(Status.SUCCEED);
	}
	
	private void svnCheckout(String url, String revision) throws Exception {

		lastRevision = getLastRevisionProperty();
		
		CheckoutInfo checkout = client.checkout(url, revision, parent.getFolder());
		currentRevision = checkout.revision;
		logger.log(Module.COMMAND, "Checkout with Revision: "+currentRevision);

		setLastRevisionProperty(currentRevision);
		setRevisionProperty(currentRevision);

		Artifact checkoutArtifact = new Artifact("Checkout", checkout.output, "txt");
		checkoutArtifact.description = "Revision: "+checkout.revision;
		artifacts.add(checkoutArtifact);
	}
	
	private void svnHistory() throws Exception {
		
		if(lastRevision != null && !lastRevision.equals(currentRevision)){
			String startRevision = client.getNextRevision(lastRevision);
			String endRevision = currentRevision;
			
			history = client.getHistory(getUrlProperty(), startRevision, endRevision);
			logger.log(Module.COMMAND, "History has "+history.commits.size()+" Committs");
			
			Artifact commitArtifact = new Artifact("Commits", history.output, "txt");
			commitArtifact.description = "Intervall: "+history.revision1+" - "+history.revision2;
			artifacts.add(commitArtifact);
		}
	}
}
