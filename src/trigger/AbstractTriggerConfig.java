package trigger;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

public abstract class AbstractTriggerConfig {

	public enum OPTIONS {
		ACTIVE
	}
	
	protected OptionContainer container;
	
	public AbstractTriggerConfig(){
		
		container = new OptionContainer();
		container.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, false
		));
	}
	
	public OptionContainer getOptionContainer(){ return container; }
	
	public String getName(){
		return getClass().getSimpleName();
	}
	
	@Override
	public String toString(){ return getName(); }
	
	public abstract AbstractTrigger createTrigger();
}
