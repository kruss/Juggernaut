package core.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import ui.dialog.PropertyInfo;
import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;
import util.DateTools;
import util.StringTools;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTrigger;
import core.launch.trigger.AbstractTriggerConfig;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.FileManager;
import core.runtime.TaskManager;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ErrorManager;
import core.runtime.smtp.ISmtpClient;


/**
 * the configuration of a launch,- will be serialized
 */
public class LaunchConfig implements Comparable<LaunchConfig>, IOptionInitializer {
	
	public enum GROUPS {
		GENERAL, NOTIFICATION
	}
	
	public enum OPTIONS {
		ACTIVE, DESCRIPTION, CLEAN, TIMEOUT, NOTIFICATION, ADMINISTRATORS, COMMITTERS, MESSAGE
	}

	private String id;
	private String name;
	private OptionContainer optionContainer;
	private ArrayList<AbstractOperationConfig> operations;
	private ArrayList<AbstractTriggerConfig> triggers;
	private transient boolean dirty;
	
	public LaunchConfig(String name){

		id = UUID.randomUUID().toString();
		this.name = name;
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription("The launch settings");
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.ACTIVE.toString(), "The launch's active state",
				Type.BOOLEAN, true
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.DESCRIPTION.toString(), "The launch's description", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.CLEAN.toString(), "Clean launch-folder on start",
				Type.BOOLEAN, true
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0, 0, 300
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.NOTIFICATION.toString(), "Perform eMail-notifications",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "List of administrator eMails (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.COMMITTERS.toString(), "Threshold for committer notification (0 = no committer)", 
				Type.INTEGER, 0, 0, 15
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.MESSAGE.toString(), "Optional notification message", 
				Type.TEXT_AREA, ""
		));
		
		operations = new ArrayList<AbstractOperationConfig>();
		triggers = new ArrayList<AbstractTriggerConfig>();
		dirty = true;
	}
	
	@Override
	public void initOptions(OptionContainer container){}
	@Override
	public void initEditor(OptionEditor editor) {
		
		ArrayList<String> properties = StringTools.enum2strings(LaunchAgent.PROPERTY.class);
		PropertyInfo info = new PropertyInfo(id, properties);
		info.setInfo(editor);
	}
	
	public void setId(String id){ this.id = id; }
	public String getId(){ return id; }
	
	public void setName(String name){ this.name = name; }
	public String getName(){ return name; }
	
	/** answers if configuration could be launched */
	public boolean isReady(){
		return isActive() && !isDirty() && isValid();
	}

	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	public String getDescription(){ 
		return optionContainer.getOption(OPTIONS.DESCRIPTION.toString()).getStringValue(); 
	}
	
	public void setActive(boolean active){ 
		optionContainer.getOption(OPTIONS.ACTIVE.toString()).setBooleanValue(active); 
	}
	
	public boolean isClean() {
		return optionContainer.getOption(OPTIONS.CLEAN.toString()).getBooleanValue();
	}
	
	/** the timout in millis */
	public long getTimeout() {
		return DateTools.min2millis(optionContainer.getOption(OPTIONS.TIMEOUT.toString()).getIntegerValue());
	}
	
	public boolean isNotification(){
		return optionContainer.getOption(OPTIONS.NOTIFICATION.toString()).getBooleanValue();
	}
	
	public ArrayList<String> getAdministratorAddresses() {
		
		String value = optionContainer.getOption(OPTIONS.ADMINISTRATORS.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	public int getCommitterThreshold() {
		return optionContainer.getOption(OPTIONS.COMMITTERS.toString()).getIntegerValue();
	}
	
	public String getSmtpMessage() {
		return optionContainer.getOption(OPTIONS.MESSAGE.toString()).getStringValue();
	}
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public boolean isValid(){
		
		for(AbstractOperationConfig config : operations){
			if(config.isActive() && !config.isValid()){ return false; }
		}
		for(AbstractTriggerConfig config : triggers){
			if(config.isActive() && !config.isValid()){ return false; }
		}
		return true;
	}
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	public ArrayList<AbstractOperationConfig> getOperationConfigs(){ return operations; }
	public ArrayList<AbstractTriggerConfig> getTriggerConfigs(){ return triggers; }
	
	@Override
	public String toString(){ 
		if(isActive()){
			return (isValid() ? "" : "~ ") + name + (isDirty() ? " *" : "");
		}else{
			return (isValid() ? "" : "~ ") + "<"+name+">" + (isDirty() ? " *" : "");
		}
	}

	@Override
	public int compareTo(LaunchConfig o) {
		return name.compareTo(o.getName());
	}
	
	/** clone configuration as it is */
	public LaunchConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		LaunchConfig config = (LaunchConfig)xstream.fromXML(xml);
		return config;
	}
	
	/** clone configuration with new id (recursive) */
	public LaunchConfig duplicate(){
		
		HashMap<String, String> map = new HashMap<String, String>();
		LaunchConfig config = this.clone();
		config.id = duplicateId(config.id, map);
		config.name = this.getName()+" (Copy)";
		config.dirty = true;
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			operationConfig.setId(duplicateId(operationConfig.getId(), map));
		}
		for(AbstractTriggerConfig triggerConfig : config.getTriggerConfigs()){
			triggerConfig.setId(duplicateId(triggerConfig.getId(), map));
		}
		return applyDuplicateIds(config, map);
	}

	private String duplicateId(String id, HashMap<String, String> map){
		String newId = UUID.randomUUID().toString();
		map.put(id, newId);
		return newId;
	}
	
	private LaunchConfig applyDuplicateIds(LaunchConfig config, HashMap<String, String> map) {
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(config);
		for(String id : map.keySet()){
			String newId = map.get(id);
			if(newId != null){
				xml = xml.replaceAll(id, newId);
			}
		}
		LaunchConfig applied = (LaunchConfig)xstream.fromXML(xml);
		return applied;
	}
	
	public LaunchAgent createLaunch(
			ErrorManager errorManager,
			Configuration configuration, 
			Cache cache,
			History history, 
			FileManager fileManager, 
			TaskManager taskManager, 
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			AbstractTrigger trigger)
	{
		return new LaunchAgent(
				errorManager, configuration, cache, history, fileManager, 
				taskManager, smtpClient, httpServer, this, trigger
		);
	}
}
