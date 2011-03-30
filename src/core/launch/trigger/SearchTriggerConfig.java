package core.launch.trigger;

import java.util.ArrayList;

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
import util.StringTools;

public class SearchTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Search";
	
	public enum OPTIONS {
		URL, REVISION_START, REVISION_END
	}

	public SearchTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION_START.toString(), "Start revision for the interval search", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION_END.toString(), "Start revision for the interval search", 
				Type.TEXT_SMALL, ""
		));
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.URL.toString()),
				new RepositoryTest(taskManager, new SVNClient(taskManager, logger), logger)
		);
	}
	
	@Override
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public String getDescription(){
		return "A trigger for SVN Repository interval search";
	}
	
	@Override
	public ArrayList<String> getPropertyNames() { 
		return StringTools.enum2strings(SearchTrigger.PROPERTY.class);
	}
	
	public String getUrl(){
		return optionContainer.getOption(OPTIONS.URL.toString()).getStringValue();
	}
	
	public String getStartRevision(){
		return optionContainer.getOption(OPTIONS.REVISION_START.toString()).getStringValue();
	}
	
	public String getEndRevision(){
		return optionContainer.getOption(OPTIONS.REVISION_END.toString()).getStringValue();
	}
	
	@Override
	public boolean isValid(){ 
		return 
			!getUrl().isEmpty() &&
			!getStartRevision().isEmpty() &&
			!getEndRevision().isEmpty() &&
			!getStartRevision().equals(getEndRevision()); 
	}
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new SearchTrigger(configuration, cache, taskManager, logger, this);
	}
}
