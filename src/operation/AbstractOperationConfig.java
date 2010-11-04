package operation;

import util.Attribute;
import util.AttributeContainer;
import util.Attribute.Type;

public abstract class AbstractOperationConfig implements IOperationConfig {

	public enum ATTRIBUTES {
		NAME, DESCRIPTION, ACTIVE, TIMEOUT
	}
	
	protected AttributeContainer container;
	
	public AbstractOperationConfig(){
		
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
				ATTRIBUTES.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0
		));
	}
	
	@Override
	public String getName(){
		return getClass().getSimpleName();
	}
	
	@Override
	public AttributeContainer getContainer(){ return container; }
}
