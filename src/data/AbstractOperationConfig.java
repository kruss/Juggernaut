package data;

import java.util.UUID;

import launch.LaunchAgent;
import logger.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.Cache;
import core.Configuration;
import core.TaskManager;

import data.Option.Type;

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
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.ACTIVE.toString(), "The operation's active state",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
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
