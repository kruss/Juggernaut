package operation;

import util.Option;
import util.Option.Type;

public class ConsoleOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Command";

	public enum OPTIONS {
		COMMAND, DIRECTORY, ARGUMENTS
	}
	
	public ConsoleOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.COMMAND.toString(), "The item's command", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DIRECTORY.toString(), "The item's directory", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ARGUMENTS.toString(), "The item's arguments",
				Type.TEXTAREA, ""
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to execute a command";
	}
	
	@Override
	public boolean isValid(){
		return 
			!optionContainer.getOption(OPTIONS.COMMAND.toString()).getStringValue().isEmpty();
	}
	
	@Override
	public AbstractOperation createOperation() {
		// TODO
		return null;
	}
}
