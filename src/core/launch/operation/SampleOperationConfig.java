package core.launch.operation;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.runtime.TaskManager;
import ui.option.Option;
import ui.option.Option.Type;
import util.DateTools;

public class SampleOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Sample";
	
	public enum OPTIONS {
		ERROR, EXCEPTION, IDLE
	}
	
	public SampleOperationConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.ERROR.toString(), "Throw an error",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.EXCEPTION.toString(), "Throw an exception",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.IDLE.toString(), "Idle time in seconds", 
				Type.INTEGER, 5, 0, 60
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getUIName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to test the framework";
	}
	
	public boolean isThrowError(){
		return optionContainer.getOption(OPTIONS.ERROR.toString()).getBooleanValue();
	}
	
	public boolean isThrowException(){
		return optionContainer.getOption(OPTIONS.EXCEPTION.toString()).getBooleanValue();
	}
	
	/** the idel-time in millis */
	public long getIdleTime(){
		return DateTools.sec2millis(optionContainer.getOption(OPTIONS.IDLE.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){ return true; }
	
	@Override
	public AbstractOperation createOperation(
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent)
	{
		return new SampleOperation(cache, taskManager, parent, this);
	}
}
