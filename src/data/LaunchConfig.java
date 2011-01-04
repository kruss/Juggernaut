package data;

import java.util.ArrayList;
import java.util.UUID;

import smtp.SmtpClient;
import smtp.ISmtpConfig.NotificationMode;
import util.DateTools;
import util.StringTools;

import launch.LaunchAgent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.Cache;
import core.Configuration;
import core.FileManager;
import core.History;
import core.TaskManager;

import data.Option.Type;


/**
 * the configuration of a launch,- will be serialized
 */
public class LaunchConfig implements Comparable<LaunchConfig>, IOptionInitializer {
	
	public enum GROUPS {
		GENERAL, NOTIFICATION
	}
	
	public enum OPTIONS {
		ACTIVE, DESCRIPTION, CLEAN, TIMEOUT, NOTIFICATION, ADMINISTRATORS, MESSAGE
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
		optionContainer.setDescription("the configuration of the launch");
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.ACTIVE.toString(), "The launch's active state",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.DESCRIPTION.toString(), "The launch's description", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.CLEAN.toString(), "Clean launch-folder on start",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0, 0, 300
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.NOTIFICATION.toString(), "The launch's notification-mode",
				Type.TEXT_LIST, StringTools.enum2strings(NotificationMode.class), NotificationMode.DISABLED.toString()
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "email-list of launch admins (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
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
	
	public NotificationMode getNotificationMode(){
		return NotificationMode.valueOf(optionContainer.getOption(OPTIONS.NOTIFICATION.toString()).getStringValue());
	}
	
	public ArrayList<String> getAdministrators() {
		
		String value = optionContainer.getOption(OPTIONS.ADMINISTRATORS.toString()).getStringValue();
		return StringTools.split(value, ", ");
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
	
	public LaunchConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		LaunchConfig config = (LaunchConfig)xstream.fromXML(xml);
		return config;
	}
	
	public LaunchAgent createLaunch(
			Configuration configuration, 
			Cache cache,
			History history, 
			FileManager fileManager, 
			TaskManager taskManager, 
			SmtpClient smtpClient,
			String trigger)
	{
		return new LaunchAgent(configuration, cache, history, fileManager, taskManager, smtpClient, this, trigger);
	}
}
