package trigger;

import util.Attribute;
import util.AttributeContainer;
import util.Attribute.Type;

public abstract class AbstractTriggerConfig implements ITriggerConfig {

	public enum ATTRIBUTES {
		ACTIVE
	}
	
	protected AttributeContainer container;
	
	public AbstractTriggerConfig(){
		
		container = new AttributeContainer();
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, false
		));
	}
	
	@Override
	public AttributeContainer getContainer(){ return container; }
	
	@Override
	public String getName(){
		return getClass().getSimpleName();
	}
}
