package data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import util.Logger;

public abstract class AbstractTrigger {

	protected AbstractTriggerConfig config;
	protected Logger logger;
	
	protected String triggerReason;
	
	public AbstractTriggerConfig getConfig(){ return config; }
	
	public AbstractTrigger(AbstractTriggerConfig config){
		
		this.config = config.clone();
		triggerReason = "UNDEFINED";
	}
	
	public String getTriggerReason(){ return triggerReason; }
	
	public abstract void init();
	public abstract boolean isTriggered();
	
	public AbstractTrigger clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractTrigger trigger = (AbstractTrigger)xstream.fromXML(xml);
		return trigger;
	}
}
