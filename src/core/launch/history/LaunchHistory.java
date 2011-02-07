package core.launch.history;


import java.io.File;
import java.util.ArrayList;

import core.Constants;
import core.html.AbstractHtmlPage;
import core.html.HtmlLink;
import core.launch.LaunchAgent;
import core.launch.data.Error;
import core.runtime.FileManager;
import core.runtime.logger.Logger;


public class LaunchHistory extends AbstractHistory {
	
	private transient LaunchAgent launch;
	private transient FileManager fileManager;
	
	public String logfile;
	public String trigger;
	public ArrayList<OperationHistory> operations;
	
	public LaunchHistory(LaunchAgent launch, FileManager fileManager){
		super(launch);
		
		this.launch = launch;
		this.fileManager = fileManager;
		
		id = launch.getConfig().getId();
		name = launch.getConfig().getName();
		description = launch.getConfig().getDescription();
		trigger = launch.getTrigger();
		operations = new ArrayList<OperationHistory>();		

	}
	
	public void init() throws Exception {
		
		start = launch.getStatusManager().getStart();
		folder = fileManager.getLaunchHistoryFolderPath(start);
		logfile = folder+File.separator+Logger.OUTPUT_FILE;
		
		super.init();
		for(OperationHistory entry : operations){
			entry.init();
		}
	}
	
	public void finish() throws Exception {
		
		for(OperationHistory entry : operations){
			entry.finish();
		}

		end = launch.getStatusManager().getEnd();
		status = launch.getStatusManager().getStatus();
		super.finish();
	}

	@Override
	protected AbstractHtmlPage getHtmlPage() {
		return new LaunchHistoryPage(
				"Launch [ "+name+" ]", 
				getIndexPath(),
				new HtmlLink("&lt;&lt;", "../"+Constants.INDEX_NAME+"["+name.hashCode()+"].htm"),
				this
		);
	}
	
	public String getIndexPath() {
		return folder+File.separator+Constants.INDEX_NAME+".htm";
	}

	public OperationHistory getOperation(String id) {
		for(OperationHistory operation : operations){
			if(operation.id.equals(id)){
				return operation;
			}
		}
		return null;
	}
	
	public ArrayList<Error> getErrors(){
		
		ArrayList<Error> errors = new ArrayList<Error>();
		for(OperationHistory operation : operations){
				errors.addAll(operation.errors);
		}
		return errors;
	}
}
