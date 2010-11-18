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
				OPTIONS.TIME.toString(), "Time in seconds to run", 
				Type.INTEGER, 5
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public AbstractOperation createOperation() {
		return new SampleOperation(this);
	}
}
