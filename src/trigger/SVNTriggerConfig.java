package trigger;


import repository.SVNClient;
import core.Application;
import ui.OptionEditor;
import util.StringTools;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.Option;
import data.OptionContainer;
import data.Option.Type;

public class SVNTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "SVN";
	
	public enum OPTIONS {
		URL, DELAY
	}
	
	public SVNTriggerConfig(){
		
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.DELAY.toString(), "Trigger delay in minutes", 
				Type.INTEGER, 5, 0, 15
		));
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.addRepositoryTest(
				container.getOption(OPTIONS.URL.toString()),
				new SVNClient(Application.getInstance().getLogger())
		);
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for SVN Repository changes";
	}
	
	public String getUrl(){
		return optionContainer.getOption(OPTIONS.URL.toString()).getStringValue();
	}
	
	/** the delay in millis */
	public long getDelay(){
		return StringTools.min2millis(optionContainer.getOption(OPTIONS.DELAY.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){ 
		return !getUrl().isEmpty(); 
	}
	
	@Override
	public AbstractTrigger createTrigger() {
		return new SVNTrigger(this);
	}
}
