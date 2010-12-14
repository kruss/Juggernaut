package operation;

import java.util.ArrayList;

import core.Application;
import core.Cache;
import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;
import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;

import launch.LaunchAgent;
import launch.PropertyContainer;
import launch.StatusManager.Status;
import data.AbstractOperation;
import data.Artifact;

public class SVNOperation extends AbstractOperation {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNOperationConfig config;
	
	public String lastRevision;
	public String currentRevision;
	public ArrayList<CommitInfo> commits;
	
	public SVNOperation(LaunchAgent parent, SVNOperationConfig config) {
		super(parent, config);
		this.config = config;
		client = new SVNClient(parent.getLogger());
	}
	
	@Override
	public String getDescription() {
		return getUrlProperty() + (currentRevision != null ? " ("+currentRevision+")" : "");
	}
	
	
	private void setLastRevision(String revision){
		Cache cache = Application.getInstance().getCache();
		cache.addProperty(
				config.getId(), Property.REVISION.toString(), revision
		);
	}
	
	private String getLastRevision(){
		Cache cache = Application.getInstance().getCache();
		return cache.getProperty(
				config.getId(), Property.REVISION.toString()
		);
	}
	
	private void setRevisionProperty(String revision) {
		parent.getPropertyContainer().addProperty(
				config.getId(), Property.REVISION.toString(), revision
		);
	}

	private String getUrlProperty() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getUrl());
	}
	
	private String getRevisionProperty() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getRevision());
	}
	
	@Override
	protected void execute() throws Exception {
		
		lastRevision = getLastRevision();
		
		CheckoutInfo checkout = client.checkout(getUrlProperty(), getRevisionProperty(), parent.getFolder());
		if(checkout.revision != null){
			currentRevision = checkout.revision;
			setLastRevision(currentRevision);
			setRevisionProperty(currentRevision);
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		Artifact checkoutArtifact = new Artifact("Checkout", checkout.output);
		checkoutArtifact.description = "Revision: "+checkout.revision;
		artifacts.add(checkoutArtifact);
		
		if(lastRevision != null && currentRevision != null && !lastRevision.equals(currentRevision)){
			HistoryInfo history = client.getHistory(getUrlProperty(), lastRevision, currentRevision);
			commits = history.commits;
			Artifact commitArtifact = new Artifact("Commits", history.output);
			commitArtifact.description = "Intervall: "+history.revision1+" - "+history.revision2;
			artifacts.add(commitArtifact);
		}
	}
}
