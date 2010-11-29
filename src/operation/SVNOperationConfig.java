package operation;

import lifecycle.LaunchAgent;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Option;
import data.Option.Type;

public class SVNOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "SVN";

	public enum OPTIONS {
		URI
	}
	
	public SVNOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.URI.toString(), "SVN Repository Uri", 
				Type.TEXT, ""
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to checkout a SVN Repository";
	}
	
	public String getUri(){
		return optionContainer.getOption(OPTIONS.URI.toString()).getStringValue();
	}
	
	@Override
	public boolean isValid(){ 
		return !getUri().isEmpty(); 
	}
	
	@Override
	public AbstractOperation createOperation(LaunchAgent parent) {
		return new SVNOperation(parent, this);
	}
}
