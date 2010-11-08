package launch;

import java.util.ArrayList;

import operation.AbstractOperationConfig;
import trigger.AbstractTriggerConfig;
import util.Option;
import util.OptionContainer;
import util.Option.Type;


public class LaunchConfig {
	
	public enum OPTIONS {
		DESCRIPTION, ACTIVE, NOTIFICATION
	}
	
	private String name;
	private OptionContainer container;
	private ArrayList<AbstractOperationConfig> operations;
	private ArrayList<AbstractTriggerConfig> triggers;
	
	public LaunchConfig(String name){

		this.name = name;
		
		container = new OptionContainer();
		container.getOptions().add(new Option(
				OPTIONS.DESCRIPTION.toString(), "The item's description", 
				Type.TEXT, ""
		));
		container.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, false
		));
		container.getOptions().add(new Option(
				OPTIONS.NOTIFICATION.toString(), "eMail list to notify (comma seperated)", 
				Type.TEXT, ""
		));
		
		
		operations = new ArrayList<AbstractOperationConfig>();
		triggers = new ArrayList<AbstractTriggerConfig>();
	}
	
	public void setName(String name){ this.name = name; }
	public String getName(){ return name; }
	
	public OptionContainer getOptionContainer(){ return container; }
	public ArrayList<AbstractOperationConfig> getOperations(){ return operations; }
	public ArrayList<AbstractTriggerConfig> getTriggers(){ return triggers; }
	
	public Launch createLaunch(){
		return Launch.initializeLaunch(this);
	}
	
	@Override
	public String toString(){ return name; }
}
