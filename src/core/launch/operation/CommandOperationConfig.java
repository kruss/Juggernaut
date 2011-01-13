package core.launch.operation;

import java.util.ArrayList;

import ui.option.Option;
import ui.option.Option.Type;
import util.StringTools;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;


public class CommandOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Command";

	public enum OPTIONS {
		COMMAND, DIRECTORY, ARGUMENTS, OUTPUT
	}
	
	public CommandOperationConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.COMMAND.toString(), "The command to execute", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.DIRECTORY.toString(), "The command's directory within the launch-folder", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.ARGUMENTS.toString(), "The command's arguments (linewise, commented with '//')",
				Type.TEXT_AREA, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.OUTPUT.toString(), "List of output-directories to collect (comma seperated)", 
				Type.TEXT, ""
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to execute a command";
	}
	
	public String getCommand(){ 
		return optionContainer.getOption(OPTIONS.COMMAND.toString()).getStringValue(); 
	}
	
	public String getDirectory(){ 
		return optionContainer.getOption(OPTIONS.DIRECTORY.toString()).getStringValue(); 
	}
	
	public String getArguments(){ 
		
		String value = optionContainer.getOption(OPTIONS.ARGUMENTS.toString()).getStringValue();
		return StringTools.join(StringTools.split(value, "\\n", "//"), " ");
	}
	
	public ArrayList<String> getOutputs(){ 
		
		String value = optionContainer.getOption(OPTIONS.OUTPUT.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	@Override
	public boolean isValid(){
		return !getCommand().isEmpty();
	}
	
	@Override
	public AbstractOperation createOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent)
	{
		return new CommandOperation(configuration, cache, taskManager, parent, this);
	}
}
