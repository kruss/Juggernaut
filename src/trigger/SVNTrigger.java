package trigger;

import java.util.Date;

import core.Cache;
import core.Configuration;
import core.TaskManager;

import repository.SVNClient;
import repository.IRepositoryClient.RevisionInfo;


import logger.Logger;
import logger.ILogConfig.Module;


import data.AbstractTrigger;

public class SVNTrigger extends AbstractTrigger {

	private enum Property { REVISION };
	
	private SVNClient client;
	private SVNTriggerConfig config;
	
	private RevisionInfo info;
	
	public SVNTrigger(Configuration configuration, Cache cache, TaskManager taskManager, Logger logger, SVNTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = config;
		client = new SVNClient(taskManager, logger);
	}
	
	private void setLastRevisionCache(String revision){
		cache.addProperty(
				config.getId(), Property.REVISION.toString(), revision
		);
	}
	
	private String getLastRevisionCache(){
		return cache.getProperty(
				config.getId(), Property.REVISION.toString()
		);
	}
	
	@Override
	public TriggerStatus isTriggered() {
		
		try{
			info = client.getInfo(config.getUrl());
			String lastRevision = getLastRevisionCache();
			Date currentDate = new Date();
			
			if(lastRevision == null){
				return new TriggerStatus(
						config.getName()+" initial run", true
				);
			}else{
				if(!lastRevision.equals(info.revision)){
					if( info.date.getTime() + config.getDelay() <= currentDate.getTime() ){
						return new TriggerStatus(
								config.getName()+" revision changed ("+info.revision+")", true
						);
					}else{
						return new TriggerStatus(
								config.getName()+" revision changed within delay", false
						);
					}
				}else{
					return new TriggerStatus(
							config.getName()+" repository idle", false
					);
				}
			}
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			return new TriggerStatus(
					e.getMessage(), false
			);
		}
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(triggered && info != null){
			setLastRevisionCache(info.revision);
		}
	}
}
