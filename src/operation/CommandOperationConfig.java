package operation;

import java.util.ArrayList;

import lifecycle.LaunchAgent;
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
				OPTIONS.COMMAND.toString(), "The command to execute", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DIRECTORY.toString(), "The command's directory within the launch-folder", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ARGUMENTS.toString(), "The command's arguments (linewise, commented with '//')",
				Type.TEXTAREA, ""
		));
		optionContainer.getOptions().add(new Option(
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
	
	public ArrayList<String> getArguments(){ 
		
		ArrayList<String> list = new ArrayList<String>();
		String arguments = optionContainer.getOption(OPTIONS.ARGUMENTS.toString()).getStringValue();
		String[] strings = arguments.split("\\n");
		for(String string : strings){
			if(!string.startsWith("//")){
				list.add("\""+string+"\"");
			}
		}
		return list;
	}
	
	public ArrayList<String> getOutputPattern(){ 
		
		ArrayList<String> list = new ArrayList<String>();
		String arguments = optionContainer.getOption(OPTIONS.ARGUMENTS.toString()).getStringValue();
		String[] strings = arguments.split(", ");
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
