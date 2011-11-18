package core.launch.trigger;

import java.util.ArrayList;

import core.launch.repository.RepositoryTest;
import core.launch.repository.SVNClient;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import ui.option.IOptionDelegate;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;
import util.StringTools;
import util.UiTools;

public class SearchTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Search";
	
	public enum OPTIONS {
		URL, REVISION_START, REVISION_END, REVISION_INTERVALL
	}

	public SearchTriggerConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION_START.toString(), "Start revision for search", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION_END.toString(), "End revision for search", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION_INTERVALL.toString(), "Revision intervall for search", 
				Type.INTEGER, 1, 1, 100
		));
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.URL.toString()),
				new RepositoryTest(taskManager, new SVNClient(taskManager, logger), logger)
		);
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.REVISION_START.toString()),
				new IOptionDelegate(){
					@Override
					public String getDelegateName() { return "Restart"; }
					@Override
					public void perform(String content) {
						if(UiTools.confirmDialog("Restart the search")){
							cache.removeValues(getId());
						}
					}
				}
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
	
	public int getRevisionIntervall(){ 
		return optionContainer.getOption(OPTIONS.REVISION_INTERVALL.toString()).getIntegerValue();
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
