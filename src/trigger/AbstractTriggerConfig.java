package trigger;

import model.Attribute;
import model.AttributeContainer;
import model.ITriggerConfig;
import model.Attribute.Type;

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
}
