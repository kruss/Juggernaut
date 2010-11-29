package trigger;

import util.StringTools;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.Option;
import data.Option.Type;

public class SVNTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "SVN";
	
	public enum OPTIONS {
		URI, DELAY
	}
	
	public SVNTriggerConfig(){
		
		optionContainer.getOptions().add(new Option(
				OPTIONS.URI.toString(), "SVN Repository Uri", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DELAY.toString(), "Trigger delay in minutes", 
				Type.INTEGER, 5, 0, 15
		));
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for SVN Repository changes";
	}
	
	public String getUri(){
		return optionContainer.getOption(OPTIONS.URI.toString()).getStringValue();
	}
	
	/** the delay in millis */
	public long getDelay(){
		return StringTools.min2millis(optionContainer.getOption(OPTIONS.DELAY.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){ 
		return !getUri().isEmpty(); 
	}
	
	@Override
	public AbstractTrigger createTrigger() {
		return new SVNTrigger(this);
	}
}
