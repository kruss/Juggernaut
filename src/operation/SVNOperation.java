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
	
	public String lastRevision;
	public String currentRevision;
	
	public HistoryInfo history;		
	
	@Override
	public String getUrl(){ return config.getUrl(); }
	@Override
	public String getRevision(){ return currentRevision; }
	@Override
	public HistoryInfo getHistory(){ return history; }
	
	public SVNOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager 
			taskManager, 
			LaunchAgent parent, 
			SVNOperationConfig config)
	{
		super(configuration, cache, taskManager, parent, config);
		this.config = (SVNOperationConfig) super.config;
		
		client = new SVNClient(taskManager,parent.getLogger());
		
		lastRevision = null;
		currentRevision = null;
		history = null;
	}
	
	@Override
	public String getDescription() {
		return config.getUrl() + (currentRevision != null ? " ("+currentRevision+")" : "");
	}
	
	private void setLastRevision(String revision){
		cache.setValue(
				config.getId(), PROPERTY.REVISION.toString(), revision
		);
	}
	
	private String getLastRevision(){
		return cache.getValue(
				config.getId(), PROPERTY.REVISION.toString()
		);
	}
	
	private void setCurrentRevision(String revision) {
		parent.getPropertyContainer().setProperty(
				new Property(config.getId(), PROPERTY.REVISION.toString(), revision)
		);
	}
	
	@Override
	protected void execute() throws Exception {
		
		svnCheckout(config.getUrl(), config.getRevision());
		try{
			svnHistory();
		}catch(Exception e){
			logger.error(Module.COMMAND, e);
		}
		
		statusManager.setStatus(Status.SUCCEED);
	}
	
	private void svnCheckout(String url, String revision) throws Exception {

		lastRevision = getLastRevision();
		
		CheckoutInfo checkout = client.checkout(url, revision, parent.getFolder());
		currentRevision = checkout.revision;
		logger.log(Module.COMMAND, "Checkout with Revision: "+currentRevision);

		setLastRevision(currentRevision);
		setCurrentRevision(currentRevision);

		Artifact checkoutArtifact = new Artifact("Checkout", checkout.output, "txt");
		checkoutArtifact.description = "Revision: "+checkout.revision;
		artifacts.add(checkoutArtifact);
	}
	
	private void svnHistory() throws Exception {
		
		if(lastRevision != null && !lastRevision.equals(currentRevision)){
			String startRevision = client.getNextRevision(lastRevision);
			String endRevision = currentRevision;
			
			history = client.getHistory(config.getUrl(), startRevision, endRevision);
			logger.log(Module.COMMAND, "History has "+history.commits.size()+" Committs");
			
			Artifact commitArtifact = new Artifact("Commits", history.output, "txt");
			commitArtifact.description = "Intervall: "+history.revision1+" - "+history.revision2;
			artifacts.add(commitArtifact);
		}
	}
}
