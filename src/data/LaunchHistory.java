package data;

import html.AbstractHtmlPage;
import html.HistoryPage;
import html.HtmlLink;
import html.LaunchHistoryPage;

import java.io.File;
import java.util.ArrayList;

import core.FileManager;

import launch.LaunchAgent;
import logger.Logger;

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
		folder = 
			fileManager.getHistoryFolderPath()+
			File.separator+start.getTime();
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
				new HtmlLink("&lt;&lt;", "../"+HistoryPage.OUTPUT_FILE),
				this
		);
	}
	
	public String getIndexPath() {
		return folder+File.separator+LaunchHistoryPage.OUTPUT_FILE;
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
