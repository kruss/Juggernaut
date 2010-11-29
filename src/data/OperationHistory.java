package data;

import java.io.File;


import core.Application;

public class OperationHistory extends AbstractHistory {
	
	public OperationHistory(AbstractOperation operation){
		
		id = operation.getConfig().getId();
		name = operation.getConfig().getName();
		description = operation.getIndex()+". Operation of the Launch";
		start = operation.getStatusManager().getStart();
		end = operation.getStatusManager().getEnd();
		status = operation.getStatusManager().getStatus();
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+operation.getParent().getStatusManager().getStart().getTime()+
			File.separator+id;
		artifacts = operation.getArtifacts();
	}
}
