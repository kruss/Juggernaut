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
	
	protected OptionContainer optionContainer;
	
	public AbstractTriggerConfig(){
		
		id = UUID.randomUUID().toString();
		
		optionContainer = new OptionContainer();
		optionContainer.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, true
		));
	}
	
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return optionContainer; }

	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return getName();
		}else{
			return "<"+getName()+">";
		}
	}
	
	public abstract String getName();
	public abstract AbstractTrigger createTrigger();
}
