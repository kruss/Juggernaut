package core.launch.trigger;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.logger.Logger;

public abstract class AbstractTrigger {
	
	protected Configuration configuration;
	protected Cache cache;
	protected AbstractTriggerConfig config;
	protected Logger logger;
	
	protected TriggerStatus status;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	public TriggerStatus getStatus(){ return status; }
	
	public AbstractTrigger(
			Configuration configuration, 
			Cache cache, 
			Logger logger,
			AbstractTriggerConfig config)
	{
		this.configuration = configuration;
		this.cache = cache;
		this.logger = logger;
		this.config = config.clone();
		
		status = new TriggerStatus("UNDEFINED", false);
	}
	
	public abstract void checkTrigger();
	public abstract void wasTriggered(LaunchAgent launch);
	
	public class TriggerStatus {
		
		public String message;
		public boolean triggered;
		
		public TriggerStatus(String message, boolean triggered){
			this.message = message;
			this.triggered = triggered;
		}
	}
}
