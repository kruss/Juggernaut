package core.launch.operation;

import java.util.UUID;

import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.Option.Type;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;


/**
 * the configuration of an operation,- will be serialized
 */
public abstract class AbstractOperationConfig implements IOptionInitializer {

	public enum GROUPS {
		GENERAL, SETTINGS
	}
	
	public enum OPTIONS {
		ACTIVE, CRITICAL
	}

	protected transient TaskManager taskManager;
	protected transient Logger logger;
	
	private String id;
	protected OptionContainer optionContainer;
	
	public AbstractOperationConfig(){
		
		id = UUID.randomUUID().toString();
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription(getDescription());
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.ACTIVE.toString(), "The operation's active state",
				Type.BOOLEAN, true
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.CRITICAL.toString(), "The operation will aboard the launch on errors",
				Type.BOOLEAN, false
		));
	}
	
	public void initInstance(TaskManager taskManager, Logger logger) {
		this.taskManager = taskManager;
		this.logger = logger;
	}
	
	@Override
	public void initOptions(OptionContainer container){}
	
	public void setId(String id){ this.id = id; }
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	
	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	public boolean isCritical() {
		return optionContainer.getOption(OPTIONS.CRITICAL.toString()).getBooleanValue(); 
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
	public abstract AbstractOperation createOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent);
	
	public AbstractOperationConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractOperationConfig config = (AbstractOperationConfig)xstream.fromXML(xml);
		return config;
	}
}
