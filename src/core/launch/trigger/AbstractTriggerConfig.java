package core.launch.trigger;

import java.util.UUID;


import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.Option.Type;



import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;



/**
 * the configuration of a trigger,- will be serialized
 */
public abstract class AbstractTriggerConfig implements IOptionInitializer {

	public enum GROUPS {
		GENERAL, SETTINGS
	}
	
	public enum OPTIONS {
		ACTIVE
	}
	
	protected transient TaskManager taskManager;
	protected transient Logger logger;
	
	private String id;
	protected OptionContainer optionContainer;
	
	public AbstractTriggerConfig(){
		
		id = UUID.randomUUID().toString();
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription(getDescription());
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.ACTIVE.toString(), "The trigger's active state",
				Type.BOOLEAN, true
		));
	}
	
	public void initInstance(TaskManager taskManager, Logger logger) {
		this.taskManager = taskManager;
		this.logger = logger;
	}
	
	@Override
	public void initOptions(OptionContainer container) {}
	
	public void setId(String id){ this.id = id; }
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return optionContainer; }

	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return (isValid() ? "" : "~ ") + getName();
		}else{
			return (isValid() ? "" : "~ ") + "<"+getName()+">";
		}
	}
	
	public abstract String getName();
	public abstract String getDescription();
	public abstract boolean isValid();
	public abstract AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger);
	
	public AbstractTriggerConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractTriggerConfig config = (AbstractTriggerConfig)xstream.fromXML(xml);
		return config;
	}
}
