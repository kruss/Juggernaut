package operation;

import core.Application;
import core.Cache;
import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;
import repository.IRepositoryClient.HistoryInfo;

import launch.LaunchAgent;
import launch.PropertyContainer;
import launch.StatusManager.Status;
import logger.Logger.Module;
import data.AbstractOperation;
import data.Artifact;

public class SVNOperation extends AbstractOperation implements IRepositoryOperation {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNOperationConfig config;
	
	public String lastRevision;
	public String currentRevision;
	public HistoryInfo history;		
	
	@Override
	public String getRevision(){ return currentRevision; }
	@Override
	public HistoryInfo getHistory(){ return history; }
	
	public SVNOperation(LaunchAgent parent, SVNOperationConfig config) {
		super(parent, config);
		this.config = config;
		client = new SVNClient(parent.getLogger());
	}
	
	@Override
	public String getDescription() {
		return getUrlProperty() + (currentRevision != null ? " ("+currentRevision+")" : "");
	}
	
	
	private void setLastRevisionCache(String revision){
		Cache cache = Application.getInstance().getCache();
		cache.addProperty(
				config.getId(), Property.REVISION.toString(), revision
		);
	}
	
	private String getLastRevisionCache(){
		Cache cache = Application.getInstance().getCache();
		return cache.getProperty(
				config.getId(), Property.REVISION.toString()
		);
	}

	private String getUrlProperty() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getUrl());
	}
	
	private String getRevisionProperty() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getRevision());
	}
	
	private void setRevisionProperty(String revision) {
		parent.getPropertyContainer().addProperty(
				config.getId(), Property.REVISION.toString(), revision
		);
	}
	
	@Override
	protected void execute() throws Exception {
		
		String url = getUrlProperty();
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
		
		lastRevision = getLastRevisionCache();

		CheckoutInfo checkout = client.checkout(url, revision, parent.getFolder());
		currentRevision = checkout.revision;
		setLastRevisionCache(currentRevision);
		setRevisionProperty(currentRevision);

		Artifact checkoutArtifact = new Artifact("Checkout", checkout.output);
		checkoutArtifact.description = "Revision: "+checkout.revision;
		artifacts.add(checkoutArtifact);
	}
	
	private void svnHistory() throws Exception {
		
		if(lastRevision != null && !lastRevision.equals(currentRevision)){
			String startRevision = client.getNextRevision(lastRevision);
			String endRevision = currentRevision;
			
			history = client.getHistory(getUrlProperty(), startRevision, endRevision);
			
			Artifact commitArtifact = new Artifact("Commits", history.output);
			commitArtifact.description = "Intervall: "+history.revision1+" - "+history.revision2;
			artifacts.add(commitArtifact);
		}
	}
}
