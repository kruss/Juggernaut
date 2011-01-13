package core.launch.trigger;

import core.launch.repository.RepositoryTest;
import core.launch.repository.SVNClient;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;
import util.DateTools;

public class SVNTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "SVN";
	
	public enum OPTIONS {
		URL, DELAY
	}
	
	public SVNTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.DELAY.toString(), "Trigger delay in minutes", 
				Type.INTEGER, 5, 0, 15
		));
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.URL.toString()),
				new RepositoryTest(
					new SVNClient(taskManager, logger), logger
				)
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
		return DateTools.min2millis(optionContainer.getOption(OPTIONS.DELAY.toString()).getIntegerValue());
	}
	
	@Override
	public boolean isValid(){ 
		return !getUrl().isEmpty(); 
	}
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new SVNTrigger(configuration, cache, taskManager, logger, this);
	}
}
