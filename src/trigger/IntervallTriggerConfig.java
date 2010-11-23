package trigger;

import util.StringTools;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.Option;
import data.Option.Type;

public class IntervallTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Intervall";
	
	public enum OPTIONS {
		DAYS, HOURS, MINUTES
	}
	
	public IntervallTriggerConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.DAYS.toString(), "Intervall in days", 
				Type.INTEGER, 0
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.HOURS.toString(), "Intervall in hours", 
				Type.INTEGER, 0
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.MINUTES.toString(), "Intervall in minutes", 
				Type.INTEGER, 0
		));
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for a cyclic intervall";
	}
	
	public long getIntervall(){
		
		return
			StringTools.day2millis(optionContainer.getOption(OPTIONS.DAYS.toString()).getIntegerValue()) +
			StringTools.hour2millis(optionContainer.getOption(OPTIONS.HOURS.toString()).getIntegerValue()) +
			StringTools.min2millis(optionContainer.getOption(OPTIONS.MINUTES.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){
		return 
			optionContainer.getOption(OPTIONS.DAYS.toString()).getIntegerValue() >= 0 &&
			optionContainer.getOption(OPTIONS.HOURS.toString()).getIntegerValue() >= 0 &&
			optionContainer.getOption(OPTIONS.MINUTES.toString()).getIntegerValue() >= 0;
	}
	
	@Override
	public AbstractTrigger createTrigger() {
		return new IntervallTrigger(this);
	}
}
