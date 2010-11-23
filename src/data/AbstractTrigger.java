package data;

import lifecycle.LaunchManager.TriggerStatus;
import util.Logger;

public abstract class AbstractTrigger {
	
	protected transient Logger logger;
	protected AbstractTriggerConfig config;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
	public AbstractTrigger(AbstractTriggerConfig config){
		
		this.config = config.clone();
	}
	
	public abstract TriggerStatus isTriggered();
	public abstract void wasTriggered(boolean triggered);
}
