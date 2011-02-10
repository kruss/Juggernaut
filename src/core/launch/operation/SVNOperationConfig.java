package core.launch.operation;


import java.util.ArrayList;

import core.launch.LaunchAgent;
import core.launch.repository.RepositoryTest;
import core.launch.repository.SVNClient;
import core.launch.repository.IRepositoryClient.Revision;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;

import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;
import util.StringTools;

public class SVNOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "SVN";
	
	public enum OPTIONS {
		URL, REVISION
	}
	
	public SVNOperationConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION.toString(), "Revision to checkout (HEAD if empty)", 
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
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getUIName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to checkout a SVN Repository";
	}
	
	@Override
	public ArrayList<String> getPropertyNames() { 
		return StringTools.enum2strings(SVNOperation.PROPERTY.class);
	}
	
	public String getUrl(){
		return optionContainer.getOption(OPTIONS.URL.toString()).getStringValue();
	}
	
	public String getRevision(){
		String revision = optionContainer.getOption(OPTIONS.REVISION.toString()).getStringValue();
		if(revision.isEmpty()){
			return Revision.HEAD.toString();
		}else{
			return revision;
		}
	}
	
	@Override
	public boolean isValid(){ 
		return !getUrl().isEmpty(); 
	}
	
	@Override
	public AbstractOperation createOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent)
	{
		return new SVNOperation(configuration, cache, taskManager, parent, this);
	}
}
