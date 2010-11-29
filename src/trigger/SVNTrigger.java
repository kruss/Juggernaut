package trigger;

import java.util.Date;
import java.util.HashMap;

import repository.SVNClient;
import repository.IRepositoryClient.RevisionInfo;

import lifecycle.LaunchManager;
import lifecycle.LaunchManager.TriggerStatus;


import data.AbstractTrigger;

public class SVNTrigger extends AbstractTrigger {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNTriggerConfig config;
	
	private RevisionInfo revisionInfo;
	
	public SVNTrigger(SVNTriggerConfig config) {
		super(config);
		this.config = config;
		client = new SVNClient(observer);
	}
	
	private void setLastRevision(String revision){
		
		HashMap<String, String> cache = launcher.getCache();
		cache.put(
				config.getId()+"::"+Property.REVISION.toString(), revision
		);
	}
	
	private String getLastRevision(){
		
		HashMap<String, String> cache = launcher.getCache();
		return cache.get(config.getId()+"::"+Property.REVISION.toString());
	}
	
	@Override
	public TriggerStatus isTriggered() {
		
		try{
			String lastRevision = getLastRevision();
			revisionInfo = client.getInfo(config.getUrl());
			Date currentDate = new Date();
			
			if(lastRevision == null){
				return LaunchManager.INITIAL_TRIGGER;
			}else{
				if(!lastRevision.equals(revisionInfo.revision)){
					if(
							(revisionInfo.date == null) ||
							(revisionInfo.date.getTime() + config.getDelay() <= currentDate.getTime())
					){
						return launcher.new TriggerStatus(
								"Repository changed: "+revisionInfo.revision, true
						);
					}else{
						return launcher.new TriggerStatus(
								"Repository changes within delay", false
						);
					}
				}else{
					return launcher.new TriggerStatus(
							"Repository not changed", false
					);
				}
			}
		}catch(Exception e){
			observer.error(e);
			return launcher.new TriggerStatus(
					e.getMessage(), false
			);
		}
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(revisionInfo != null){
			setLastRevision(revisionInfo.revision);
		}
	}
}
