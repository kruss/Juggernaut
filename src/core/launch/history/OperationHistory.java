package core.launch.history;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;


import core.html.AbstractHtmlPage;
import core.html.HtmlLink;
import core.launch.data.Error;
import core.launch.operation.AbstractOperation;
import core.runtime.FileManager;

public class OperationHistory extends AbstractHistory {
	
	private transient AbstractOperation operation;
	private transient FileManager fileManager;
	
	public int index;
	public ArrayList<Error> errors;
	
	public OperationHistory(AbstractOperation operation, FileManager fileManager){
		super(operation);
		
		this.operation = operation;
		this.fileManager = fileManager;
		
		id = operation.getConfig().getId();
		name = operation.getConfig().getName();
		index = operation.getIndex();
		errors = new ArrayList<Error>();
	}
	
	public void init() throws Exception {	
		
		Date start = operation.getParent().getStatusManager().getStart();
		folder = fileManager.getLaunchHistoryFolderPath(start)+File.separator+id;
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