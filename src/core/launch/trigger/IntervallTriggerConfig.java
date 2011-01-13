package core.launch.trigger;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import ui.option.Option;
import ui.option.Option.Type;
import util.DateTools;

public class IntervallTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Intervall";
	
	public enum OPTIONS {
		DAYS, HOURS, MINUTES
	}
	
	public IntervallTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.DAYS.toString(), "Intervall in days", 
				Type.INTEGER, 0, 0, 356
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.HOURS.toString(), "Intervall in hours", 
				Type.INTEGER, 0, 0, 23
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.MINUTES.toString(), "Intervall in minutes", 
				Type.INTEGER, 0, 0, 59
		));
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for a cyclic intervall";
	}
	
	/** the total interval in millis */
	public long getIntervall(){
		
		return
			DateTools.day2millis(optionContainer.getOption(OPTIONS.DAYS.toString()).getIntegerValue()) +
			DateTools.hour2millis(optionContainer.getOption(OPTIONS.HOURS.toString()).getIntegerValue()) +
			DateTools.min2millis(optionContainer.getOption(OPTIONS.MINUTES.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){ return true; }
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new IntervallTrigger(configuration, cache, logger, this);
	}
}
