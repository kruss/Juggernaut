package lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import lifecycle.LaunchManager.LaunchStatus;
import lifecycle.LaunchManager.TriggerStatus;

import core.Application;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.LaunchConfig;
import util.Task;

public class SchedulerTask extends Task {

	public static final long TIMEOUT = 60 * 60 * 1000; // 1h
	
	private Application application;
	
	public SchedulerTask(){
		
		super("Scheduler", Application.getInstance().getLogger());
		this.application = Application.getInstance();
	}

	@Override
	protected void runTask() {
		
		setCycle(application.getConfiguration().getSchedulerIntervall());
		checkSchedules();
	}

	public void checkSchedules() {
		
		application.getLogger().debug("Checking schedules");
		application.getLaunchManager().setLastUpdate(new Date());
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
		
		boolean launched = false;
		for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
			AbstractTrigger trigger = triggerConfig.createTrigger();
			TriggerStatus triggerStatus = trigger.isTriggered();
			if(triggerStatus.triggered){
				if(!launched)
				{
					LaunchAgent launch = launchConfig.createLaunch(triggerStatus);
					LaunchStatus launchStatus = application.getLaunchManager().runLaunch(launch);
					if(launchStatus.launched){
						application.getLogger().log(
								"Launch ["+launchConfig.getName()+"] triggered: "+triggerStatus.message
						);
						trigger.wasTriggered(true);
						launched = true;
					}else{
						application.getLogger().log(
								"Launch ["+launchConfig.getName()+"] aborded: "+launchStatus.message
						);
						trigger.wasTriggered(false);
						break;
					}
				}else{
					trigger.wasTriggered(true);
				}
			}else{
				application.getLogger().debug(
						"Trigger ["+triggerConfig.getId()+"] idle: "+triggerStatus.message
				);
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
