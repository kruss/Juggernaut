package trigger;

import java.util.Date;

import core.Application;
import core.Cache;

import repository.SVNClient;
import repository.IRepositoryClient.RevisionInfo;

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
	
	@Override
	public TriggerStatus isTriggered() {
		
		try{
			String lastRevision = getLastRevision();
			revisionInfo = client.getInfo(config.getUrl());
			Date currentDate = new Date();
			
			if(lastRevision == null){
				return launcher.new TriggerStatus(
						config.getName()+" (Initial run)", true
				);
			}else{
				if(!lastRevision.equals(revisionInfo.revision)){
					if(
							(revisionInfo.date == null) ||
							(revisionInfo.date.getTime() + config.getDelay() <= currentDate.getTime())
					){
						return launcher.new TriggerStatus(
								config.getName()+" (Revision "+revisionInfo.revision+")", true
						);
					}else{
						return launcher.new TriggerStatus(
								config.getName()+" (Changes within delay)", false
						);
					}
				}else{
					return launcher.new TriggerStatus(
							config.getName()+" (Repository idle)", false
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
