package core.runtime;

import java.util.Calendar;
import java.util.Date;

import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;

/**
 * the configuration of the application,- will be serialized
 */
public class MaintenanceConfig implements IOptionInitializer {

	public enum GROUPS {
		MAINTENANCE
	}

	public enum OPTIONS {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}
	
	private OptionContainer optionContainer;
	private transient boolean dirty;

	public MaintenanceConfig(){
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription("Setup maintenance periods");

		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.MONDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.TUESDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.WEDNESDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.THURSDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.FRIDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.SATURDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.MAINTENANCE.toString(),
				OPTIONS.SUNDAY.toString(), "Set maintenance on day of week", 
				Type.BOOLEAN, false
		));
		
		dirty = true;
	}
	
	/** answers if currently is maintenance period */
	public boolean isMaintenanceToday(Date currentDate){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2; // monday = 0
		return getDays()[currentDay];
	}
	
	/** the days of the week when maintenance is active */
	private boolean[] getDays(){
		
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
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }

	@Override
	public void initOptions(OptionContainer container) {}
	@Override
	public void initEditor(OptionEditor editor) {}
}
