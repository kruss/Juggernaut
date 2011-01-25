package core.launch.trigger;

import ui.option.Option;
import ui.option.Option.Type;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;

public class TimedTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Timed";
	
	public enum OPTIONS {
		TIME, LIMIT,
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}
	
	public TimedTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.TIME.toString(), "Run timed schedule at hour of day", 
				Type.INTEGER, 0, 0, 23
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.LIMIT.toString(), "Limit for off-time in hours (0 = no limit)", 
				Type.INTEGER, 0, 0, 3
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.MONDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.TUESDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.WEDNESDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.THURSDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.FRIDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.SATURDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.SUNDAY.toString(), "Run timed schedule on day of week", 
				Type.BOOLEAN, false
		));
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for timed launch";
	}
	
	@Override
	public boolean isValid() {
		// TODO
		return true;
	}
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new TimedTrigger(configuration, cache, logger, this);
	}
}
