package operation;

import util.Option;
import util.Option.Type;

public class ConsoleOperationConfig extends AbstractOperationConfig {

	public enum OPTIONS {
		COMMAND, DIRECTORY, ARGUMENTS
	}
	
	public ConsoleOperationConfig(){
		
		container.getOptions().add(new Option(
				OPTIONS.COMMAND.toString(), "The item's command", 
				Type.TEXT, "new "+getClass().getSimpleName()
		));
		container.getOptions().add(new Option(
				OPTIONS.DIRECTORY.toString(), "The item's directory", 
				Type.TEXT, ""
		));
		container.getOptions().add(new Option(
				OPTIONS.ARGUMENTS.toString(), "The item's arguments",
				Type.TEXTAREA, false
		));
	}
	
	@Override
	public AbstractOperation createOperation() {
		// TODO
		// return (new OperationInitializer(this)).createInstance();
		return null;
	}
}
