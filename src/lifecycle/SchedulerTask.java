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

public class SchedulerTask extends Task {

	private Application application;
	
	public SchedulerTask(){
		
		this.application = Application.getInstance();
		setName("Scheduler");
		cyclic = true;
	}

	@Override
	protected void runTask() {
		
		checkSchedules();
		cyclicDelay = application.getConfiguration().getSchedulerIntervall();
	}

	public synchronized void checkSchedules() {
		
		application.getLogger().debug("Checking schedules");
		ArrayList<LaunchConfig> launchConfigs = getRandomizedLaunchConfigs();
		for(LaunchConfig launchConfig : launchConfigs){
			if(application.getLaunchManager().isReady()){
				checkSchedules(launchConfig);
			}else{
				break;
			}
		}
	}

	private void checkSchedules(LaunchConfig launchConfig) {
		
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
	
	private ArrayList<LaunchConfig> getRandomizedLaunchConfigs(){
		
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
