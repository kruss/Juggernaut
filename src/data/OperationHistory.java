package data;

import html.AbstractHtmlPage;
import html.HtmlLink;
import html.LaunchHistoryPage;
import html.OperationHistoryPage;

import java.io.File;
import java.util.ArrayList;

import core.FileManager;

public class OperationHistory extends AbstractHistory {
	
	private transient AbstractOperation operation;
	private transient FileManager fileManager;
	
	public ArrayList<Error> errors;
	
	public OperationHistory(AbstractOperation operation, FileManager fileManager){
		super(operation);
		
		this.operation = operation;
		this.fileManager = fileManager;
		
		id = operation.getConfig().getId();
		name = operation.getConfig().getName();
		errors = new ArrayList<Error>();
	}
	
	public void init() throws Exception {	
		
		folder = 
			fileManager.getHistoryFolderPath()+
			File.separator+operation.getParent().getStatusManager().getStart().getTime()+
			File.separator+id;
		super.init();
	}
	
	public void finish() throws Exception {
		
		description = operation.getDescription();
		start = operation.getStatusManager().getStart();
		end = operation.getStatusManager().getEnd();
		status = operation.getStatusManager().getStatus();
		errors = operation.getErrors();
		super.finish();
	}
	
	@Override
	protected AbstractHtmlPage getHtmlPage() {
		return new OperationHistoryPage(
				"Operation [ "+name+" ]", 
				getIndexPath(),
				new HtmlLink("&lt;&lt;", "../"+LaunchHistoryPage.OUTPUT_FILE),
				this
		);
	}
	
	public String getIndexPath() {
		return folder+File.separator+OperationHistoryPage.OUTPUT_FILE;
	}
}
