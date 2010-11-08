package operation;

import java.util.UUID;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

public abstract class AbstractOperationConfig {

	public enum OPTIONS {
		DESCRIPTION, ACTIVE, TIMEOUT
	}
	
	private String id;
	protected OptionContainer container;
	
	public AbstractOperationConfig(){
		
		id = UUID.randomUUID().toString();
		
		container = new OptionContainer();
		container.getOptions().add(new Option(
				OPTIONS.DESCRIPTION.toString(), "The item's description", 
				Type.TEXT, ""
		));
		container.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, true
		));
		container.getOptions().add(new Option(
				OPTIONS.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0
		));
	}
	
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return container; }
	
	public String getName(){
		return getClass().getSimpleName().replaceAll("Config$", "");
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
	
	public abstract AbstractOperation createOperation();
}
