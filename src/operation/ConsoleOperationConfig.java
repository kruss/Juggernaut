package operation;

import util.Attribute;
import util.Attribute.Type;

public class ConsoleOperationConfig extends AbstractOperationConfig {

	public enum ATTRIBUTES {
		COMMAND, DIRECTORY, ARGUMENTS
	}
	
	public ConsoleOperationConfig(){
		
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.COMMAND.toString(), "The item's command", 
				Type.TEXT, "new "+getClass().getSimpleName()
		));
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.DIRECTORY.toString(), "The item's directory", 
				Type.TEXT, ""
		));
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.ARGUMENTS.toString(), "The item's arguments",
				Type.TEXTFIELD, false
		));
	}
	
	@Override
	public IOperation createInstance() {
		// TODO
		// return (new OperationInitializer(this)).createInstance();
		return null;
	}
}
