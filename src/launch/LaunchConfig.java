package launch;

import java.util.ArrayList;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


import operation.AbstractOperationConfig;
import trigger.AbstractTriggerConfig;
import util.Option;
import util.OptionContainer;
import util.Option.Type;


public class LaunchConfig implements Comparable<LaunchConfig> {
	
	public enum OPTIONS {
		ACTIVE, DESCRIPTION, CLEAN, NOTIFY, ADMINISTRATORS
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
		optionContainer.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DESCRIPTION.toString(), "The item's description", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.CLEAN.toString(), "Clean launch folder on start",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.NOTIFY.toString(), "Enable status notification",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ADMINISTRATORS.toString(), "eMail-list of administrators (comma seperated)", 
				Type.TEXT, ""
		));
		
		operations = new ArrayList<AbstractOperationConfig>();
		triggers = new ArrayList<AbstractTriggerConfig>();
		dirty = true;
	}
	
	public String getId(){ return id; }
	
	public boolean isDirty(){ return dirty; }
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	
	/** answers if configuration is ready and could be launched */
	public boolean isReady(){
		return isActive() && !dirty;
	}
	
	public void setName(String name){ this.name = name; }
	public String getName(){ return name; }
	
	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	public void setActive(boolean active){ 
		optionContainer.getOption(OPTIONS.ACTIVE.toString()).setBooleanValue(active); 
	}
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	public ArrayList<AbstractOperationConfig> getOperationConfigs(){ return operations; }
	public ArrayList<AbstractTriggerConfig> getTriggerConfigs(){ return triggers; }
	
	public LaunchAction createLaunch(){
		return new LaunchAction(this);
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return name + (dirty ? " *" : "");
		}else{
			return "<"+name+">" + (dirty ? " *" : "");
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
}
