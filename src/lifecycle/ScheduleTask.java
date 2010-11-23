package lifecycle;

import java.util.ArrayList;
import java.util.Collections;

import lifecycle.LaunchManager.LaunchStatus;
import lifecycle.LaunchManager.TriggerStatus;

import core.Application;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.LaunchConfig;
import util.Task;

public class ScheduleTask extends Task {

	private Application application;
	
	public ScheduleTask(){
		
		this.application = Application.getInstance();
		setName("Scheduler");
		cyclic = true;
	}

	@Override
	protected void runTask() {
		
		application.getLogger().debug("Checking schedules");
		ArrayList<LaunchConfig> launchConfigs = getRandomizedConfigurations();
		for(LaunchConfig launchConfig : launchConfigs){
			if(application.getLaunchManager().isReady()){
				triggerLaunch(launchConfig);
			}else{
				break;
			}
		}

		cyclicDelay = application.getConfiguration().getSchedulerIntervall();
	}

	private void triggerLaunch(LaunchConfig launchConfig) {
		
		for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
			AbstractTrigger trigger = triggerConfig.createTrigger();
			TriggerStatus triggerStatus = trigger.isTriggered();
			if(triggerStatus.triggered){
				application.getLogger().log(
						"Launch ["+launchConfig.getName()+"] triggered: "+triggerStatus.message
				);
				LaunchAgent launch = launchConfig.createLaunch(triggerStatus);
				LaunchStatus launchStatus = application.getLaunchManager().runLaunch(launch);
				if(launchStatus.launched){
					trigger.wasTriggered(true);
				}else{
					application.getLogger().log(
							"Launch ["+launchConfig.getName()+"] aborded: "+launchStatus.message
					);
					trigger.wasTriggered(false);
				}
			}
		}
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
