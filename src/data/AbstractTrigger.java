package data;

import core.Application;
import lifecycle.LaunchManager;
import lifecycle.LaunchManager.TriggerStatus;
import util.Logger;

public abstract class AbstractTrigger {
	
	protected AbstractTriggerConfig config;
	protected LaunchManager launcher;
	protected transient Logger observer;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
	public AbstractTrigger(AbstractTriggerConfig config){
		
		this.config = config.clone();
		Application application = Application.getInstance();
		launcher = application.getLaunchManager();
		observer = application.getLogger();
	}
	
	public abstract TriggerStatus isTriggered();
	public abstract void wasTriggered(boolean triggered);
}
