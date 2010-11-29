package data;

import java.io.File;


import core.Application;

public class OperationHistory extends AbstractHistory {
	
	private transient AbstractOperation operation;
	
	public OperationHistory(AbstractOperation operation){
		
		this.operation = operation;
		
		id = operation.getConfig().getId();
		name = operation.getConfig().getName();
		description = operation.getIndex()+". Operation of the Launch";
	}
	
	public void init() throws Exception {	
		
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+operation.getParent().getStatusManager().getStart().getTime()+
			File.separator+id;

		super.init();
	}
	
	public void finish() throws Exception {
		
		start = operation.getStatusManager().getStart();
		end = operation.getStatusManager().getEnd();
		status = operation.getStatusManager().getStatus();
		artifacts = operation.getArtifacts();
		
		super.finish();
	}
}
