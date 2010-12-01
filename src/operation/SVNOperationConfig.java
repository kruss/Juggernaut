package operation;

import core.Application;

import repository.SVNClient;
import repository.IRepositoryClient.Revision;
import ui.OptionEditor;
import lifecycle.LaunchAgent;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Option;
import data.OptionContainer;
import data.Option.Type;

public class SVNOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "SVN";
	
	public enum OPTIONS {
		URL, REVISION
	}
	
	public SVNOperationConfig(){
		
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.URL.toString(), "SVN Repository Url", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.REVISION.toString(), "Revision to checkout (HEAD if empty)", 
				Type.TEXT_SMALL, ""
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
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to checkout a SVN Repository";
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
	public AbstractOperation createOperation(LaunchAgent parent) {
		return new SVNOperation(parent, this);
	}
}
