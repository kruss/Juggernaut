package lifecycle;

import java.util.ArrayList;
import java.util.Collections;

import lifecycle.LaunchManager.LaunchStatus;
import lifecycle.LaunchManager.TriggerStatus;

import core.Application;
import core.Constants;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.LaunchConfig;
import util.Task;

public class ScheduleTask extends Task {

	private Application application;
	
	public ScheduleTask(){
		
		setCyclic(Constants.SCHEDULE_DELAY);
		this.application = Application.getInstance();
	}
	
	@Override
	protected void runTask() {
		
		application.getLogger().debug("Checking schedules");
		ArrayList<LaunchConfig> launchConfigs = getRandomizedConfigurations();
		while(application.getLaunchManager().isReady()){
			boolean idle = true;
			for(LaunchConfig launchConfig : launchConfigs){
				if(triggerLaunch(launchConfig)){
					idle = false;
				}
			}
			if(idle){
				break;
			}
		}
	}

	private boolean triggerLaunch(LaunchConfig launchConfig) {
		
		for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
			AbstractTrigger trigger = triggerConfig.createTrigger();
			TriggerStatus triggerStatus = trigger.isTriggered();
			if(triggerStatus.triggered){
				LaunchAgent launch = launchConfig.createLaunch(triggerStatus);
				LaunchStatus launchStatus = application.getLaunchManager().runLaunch(launch);
				if(launchStatus.launched){
					application.getLogger().log(
							"Trigger for Launch ["+launchConfig.getName()+"] fired: "+triggerStatus.message
					);
					trigger.wasTriggered(true);
					return true;
				}else{
					application.getLogger().log(
							"Trigger for Launch ["+launchConfig.getName()+"] aborded: "+launchStatus.message
					);
					trigger.wasTriggered(false);
					return false;
				}
			}
		}
		return false;
	}
	
	public ArrayList<LaunchConfig> getRandomizedConfigurations(){
		
		ArrayList<LaunchConfig> configs = new ArrayList<LaunchConfig>();
		for(LaunchConfig config : application.getConfiguration().getLaunchConfigs()){
			if(config.isReady()){
				configs.add(config);
			}
		}
		Collections.shuffle(configs);
		return configs;
	}
}
