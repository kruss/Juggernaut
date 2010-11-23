package data;

import core.Application;
import lifecycle.LaunchManager.TriggerStatus;
import util.Logger;

public abstract class AbstractTrigger {
	
	protected transient Logger logger;
	protected AbstractTriggerConfig config;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
	public AbstractTrigger(AbstractTriggerConfig config){
		
		this.config = config.clone();
		logger = Application.getInstance().getLogger();
	}
	
	public abstract TriggerStatus isTriggered();
	public abstract void wasTriggered(boolean triggered);
}
