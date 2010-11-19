package operation;

import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Option;
import data.Option.Type;

public class ConsoleOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Command";

	public enum OPTIONS {
		COMMAND, DIRECTORY, ARGUMENTS, OUTPUT
	}
	
	public ConsoleOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.COMMAND.toString(), "The command to execute", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DIRECTORY.toString(), "The command's directory within the launch-folder", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ARGUMENTS.toString(), "The item's arguments",
				Type.TEXTAREA, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.OUTPUT.toString(), "List of glob pattern to collect output (comma seperated)", 
				Type.TEXT, ""
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
