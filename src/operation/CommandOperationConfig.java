package operation;

import java.util.ArrayList;

import launch.LaunchAgent;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Option;
import data.Option.Type;

public class CommandOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Command";

	public enum OPTIONS {
		COMMAND, DIRECTORY, ARGUMENTS, OUTPUT
	}
	
	public CommandOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.COMMAND.toString(), "The command to execute", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.DIRECTORY.toString(), "The command's directory within the launch-folder", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.ARGUMENTS.toString(), "The command's arguments (linewise, commented with '//')",
				Type.TEXT_AREA, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.OUTPUT.toString(), "List of glob-pattern to collect output (comma seperated)", 
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
		
		StringBuilder arguments = new StringBuilder();
		String value = optionContainer.getOption(OPTIONS.ARGUMENTS.toString()).getStringValue();
		String[] strings = value.split("\\n");
		for(int i=0; i<strings.length; i++){
			if(!strings[i].startsWith("//")){
				if(arguments.length() > 0){ 
					arguments.append(" "); 
				}
				arguments.append(strings[i]);
			}
		}
		return arguments.toString();
	}
	
	public ArrayList<String> getGlobPattern(){ 
		
		ArrayList<String> list = new ArrayList<String>();
		String value = optionContainer.getOption(OPTIONS.OUTPUT.toString()).getStringValue();
		String[] strings = value.split(", ");
		for(String string : strings){
				list.add(string);
		}
		return list;
	}
	
	@Override
	public boolean isValid(){
		return !getCommand().isEmpty();
	}
	
	@Override
	public AbstractOperation createOperation(LaunchAgent parent) {
		return new CommandOperation(parent, this);
	}
}
