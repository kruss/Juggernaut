package data;

import core.Application;
import logger.Logger;

public abstract class AbstractTrigger {
	
	public static String USER_TRIGGER = "Run by user";
	
	protected AbstractTriggerConfig config;
	protected transient Logger observer;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
	public AbstractTrigger(AbstractTriggerConfig config){
		
		this.config = config.clone();
		observer = Application.getInstance().getLogger();
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
