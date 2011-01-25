package core.launch.trigger;

import java.util.Calendar;
import java.util.Date;

import ui.option.Option;
import ui.option.Option.Type;
import util.DateTools;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;

public class TimeTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Time";
	
	public enum OPTIONS {
		TIME, LIMIT,
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}
	
	public TimeTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.TIME.toString(), "Set starting time for schedule", 
				Type.TIME, ""+(new Date()).getTime()
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.LIMIT.toString(), "Set off-time limit in minutes (0 = no limit)", 
				Type.INTEGER, 0, 0, 180
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.MONDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.TUESDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.WEDNESDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.THURSDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.FRIDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.SATURDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.SUNDAY.toString(), "Run schedule on day of week", 
				Type.BOOLEAN, false
		));
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for a timed launch";
	}
	
	@Override
	public boolean isValid() {
		
		boolean[] days = getDays();
		for(boolean day : days){
			if(day){ 
				return true; 
			}
		}
		return false;
	}
	
	
	/** the time of the day when the trigger is active in millis */
	public long getTime() {

		long value = (new Long(optionContainer.getOption(OPTIONS.TIME.toString()).getStringValue())).longValue();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(value));
		return 	
			DateTools.hour2millis(calendar.get(Calendar.HOUR_OF_DAY)) + 
			DateTools.min2millis(calendar.get(Calendar.MINUTE));
	}
	
	/** the limit in millis for being scheduled off-time */
	public long getLimit(){
		return DateTools.min2millis(optionContainer.getOption(OPTIONS.LIMIT.toString()).getIntegerValue());
	}

	/** the days of the week when the trigger is active */
	public boolean[] getDays(){
		
		boolean[] days = new boolean[7];
		days[0] = optionContainer.getOption(OPTIONS.MONDAY.toString()).getBooleanValue();
		days[1] = optionContainer.getOption(OPTIONS.TUESDAY.toString()).getBooleanValue();
		days[2] = optionContainer.getOption(OPTIONS.WEDNESDAY.toString()).getBooleanValue();
		days[3] = optionContainer.getOption(OPTIONS.THURSDAY.toString()).getBooleanValue();
		days[4] = optionContainer.getOption(OPTIONS.FRIDAY.toString()).getBooleanValue();
		days[5] = optionContainer.getOption(OPTIONS.SATURDAY.toString()).getBooleanValue();
		days[6] = optionContainer.getOption(OPTIONS.SUNDAY.toString()).getBooleanValue();
		return days;
	}
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new TimeTrigger(configuration, cache, logger, this);
	}
}
