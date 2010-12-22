package data;

import core.Cache;
import core.Configuration;
import logger.Logger;

public abstract class AbstractTrigger {
	
	public static String USER_TRIGGER = "Run by user";
	
	protected Configuration configuration;
	protected Cache cache;
	protected AbstractTriggerConfig config;
	protected Logger logger;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
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
	}
	
	public abstract TriggerStatus isTriggered();
	public abstract void wasTriggered(boolean triggered);
	
	public class TriggerStatus {
		
		public String message;
		public boolean triggered;
		
		public TriggerStatus(String message, boolean triggered){
			this.message = message;
			this.triggered = triggered;
		}
	}
}
