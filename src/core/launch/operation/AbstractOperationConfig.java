package core.launch.operation;

import java.util.ArrayList;
import java.util.UUID;

import ui.dialog.PropertyInfo;
import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.launch.LaunchAgent;
import core.persistence.Cache;
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
	@Override
	public void initEditor(OptionEditor editor) {
		
		ArrayList<String> properties = getPropertyNames();
		if(properties.size() > 0){
			PropertyInfo info = new PropertyInfo(id, properties);
			info.setInfo(editor);
		}
	}

	public void setId(String id){ this.id = id; }
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	public ArrayList<String> getPropertyNames() { return new ArrayList<String>(); }
	
	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	public boolean isCritical() {
		return optionContainer.getOption(OPTIONS.CRITICAL.toString()).getBooleanValue(); 
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return (isValid() ? "" : "~ ") + getUIName();
		}else{
			return (isValid() ? "" : "~ ") + "<"+getUIName()+">";
		}
	}
	
	public abstract String getName();
	public abstract String getUIName();
	public abstract String getDescription();
	public abstract boolean isValid();
	public abstract AbstractOperation createOperation(
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent);
	
	/** clone configuration as it is */
	public AbstractOperationConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractOperationConfig config = (AbstractOperationConfig)xstream.fromXML(xml);
		return config;
	}
}
