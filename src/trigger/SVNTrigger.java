package trigger;

import java.util.Date;

import core.Application;
import core.Cache;

import repository.SVNClient;
import repository.IRepositoryClient.RevisionInfo;

import launch.LaunchManager.TriggerStatus;
import logger.Logger.Module;


import data.AbstractTrigger;

public class SVNTrigger extends AbstractTrigger {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNTriggerConfig config;
	
	private RevisionInfo info;
	
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
			info = client.getInfo(config.getUrl());
			String lastRevision = getLastRevision();
			Date currentDate = new Date();
			
			if(lastRevision == null){
				return launcher.new TriggerStatus(
						config.getName()+" initial run", true
				);
			}else{
				if(!lastRevision.equals(info.revision)){
					if( info.date.getTime() + config.getDelay() <= currentDate.getTime() ){
						return launcher.new TriggerStatus(
								config.getName()+" revision changed ("+info.revision+")", true
						);
					}else{
						return launcher.new TriggerStatus(
								config.getName()+" revision changed within delay", false
						);
					}
				}else{
					return launcher.new TriggerStatus(
							config.getName()+" repository idle", false
					);
				}
			}
		}catch(Exception e){
			observer.error(Module.APP, e);
			return launcher.new TriggerStatus(
					e.getMessage(), false
			);
		}
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(triggered && info != null){
			setLastRevision(info.revision);
		}
	}
}
