package trigger;


import java.util.UUID;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

public abstract class AbstractTriggerConfig {

	public enum OPTIONS {
		ACTIVE
	}
	
	private String id;
	
	protected OptionContainer container;
	
	public AbstractTriggerConfig(){
		
		id = UUID.randomUUID().toString();
		
		container = new OptionContainer();
		container.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, true
		));
	}
	
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return container; }
	
	public String getName(){
		return getClass().getSimpleName();
	}

	public boolean isActive(){ 
		return container.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return getName();
		}else{
			return "<"+getName()+">";
		}
	}
	
	public abstract AbstractTrigger createTrigger();
}
