package operation;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

public abstract class AbstractOperationConfig {

	public enum OPTIONS {
		DESCRIPTION, ACTIVE, TIMEOUT
	}
	
	protected OptionContainer container;
	
	public AbstractOperationConfig(){
		
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
				OPTIONS.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0
		));
	}
	
	public OptionContainer getOptionContainer(){ return container; }
	
	public String getName(){
		return getClass().getSimpleName();
	}
	
	public String toString(){ return getName(); }
	
	public abstract AbstractOperation createOperation();
}
