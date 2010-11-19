package operation;

import util.Option;
import util.Option.Type;

public class SampleOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Sample";

	public enum OPTIONS {
		WARNING, ERROR, EXCEPTION, TIME
	}
	
	public SampleOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.WARNING.toString(), "Throw a warning",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ERROR.toString(), "Throw an error",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.EXCEPTION.toString(), "Throw an exception",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.TIME.toString(), "Idle time in seconds", 
				Type.INTEGER, 0
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to test the framework";
	}
	
	@Override
	public boolean isValid(){
		return
			optionContainer.getOption(OPTIONS.TIME.toString()).getIntegerValue() >= 0;
	}
	
	@Override
	public AbstractOperation createOperation() {
		return new SampleOperation(this);
	}
}
