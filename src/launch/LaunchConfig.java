package launch;

import java.util.ArrayList;

import operation.IOperationConfig;
import trigger.ITriggerConfig;
import util.Attribute;
import util.AttributeContainer;
import util.Attribute.Type;


public class LaunchConfig implements ILaunchConfig {
	
	public enum ATTRIBUTES {
		NAME, DESCRIPTION, ACTIVE, NOTIFICATION
	}
	
	private AttributeContainer container;
	private ArrayList<IOperationConfig> operations;
	private ArrayList<ITriggerConfig> triggers;

	private transient boolean dirty;
	
	public LaunchConfig(){

		container = new AttributeContainer();
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.NAME.toString(), "The item's name", 
				Type.TEXT, "new "+getClass().getSimpleName()
		));
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.DESCRIPTION.toString(), "The item's description", 
				Type.TEXT, ""
		));
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, false
		));
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.NOTIFICATION.toString(), "eMail list to notify (comma seperated)", 
				Type.TEXT, ""
		));
		
		
		operations = new ArrayList<IOperationConfig>();
		triggers = new ArrayList<ITriggerConfig>();
		
		dirty = true;
	}
	
	@Override
	public AttributeContainer getContainer(){ return container; }
	@Override
	public ArrayList<IOperationConfig> getOperations(){ return operations; }
	@Override
	public ArrayList<ITriggerConfig> getTriggers(){ return triggers; }
	
	@Override
	public boolean isDirty(){ return dirty; }
	@Override
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	
	@Override
	public ILaunch createInstance(){
		// TODO
		// return (new LaunchInitializer(this)).createInstance();
		return null;
	}
}
