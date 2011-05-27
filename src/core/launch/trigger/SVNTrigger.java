package core.launch.trigger;

import java.util.Date;


import core.launch.LaunchAgent;
import core.launch.repository.SVNClient;
import core.launch.repository.IRepositoryClient.RevisionInfo;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class SVNTrigger extends AbstractTrigger {

	public static final int INFO_TIMEOUT = 30 * 1000; // 30 sec
	private enum PROPERTY { REVISION }
	
	private SVNClient client;
	private SVNTriggerConfig config;
	
	private RevisionInfo info;
	
	public SVNTrigger(Configuration configuration, Cache cache, TaskManager taskManager, Logger logger, SVNTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (SVNTriggerConfig) super.config;
		
		client = new SVNClient(taskManager, logger);
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
	
	@Override
	public void checkTrigger() {
		
		try{
			info = client.getInfo(config.getUrl(), INFO_TIMEOUT);
			String lastRevision = getLastRevision();
			Date currentDate = new Date();
			
			if(lastRevision == null){
				status = new TriggerStatus(
						config.getName()+" initial run", true
				);
			}else{
				if(!lastRevision.equals(info.revision)){
					if( info.date.getTime() + config.getDelay() <= currentDate.getTime() ){
						status = new TriggerStatus(
								config.getName()+" revision changed ("+info.revision+")", true
						);
					}else{
						status = new TriggerStatus(
								config.getName()+" revision changed within delay", false
						);
					}
				}else{
					status = new TriggerStatus(
							config.getName()+" revision not changed", false
					);
				}
			}
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			status = new TriggerStatus(
					e.getMessage(), false
			);
		}
	}

	@Override
	public void wasTriggered(LaunchAgent launch) {
		
		if(info != null){
			setLastRevision(info.revision);
		}
	}
}
