package data;

import java.io.File;
import java.util.ArrayList;


import core.Application;

import lifecycle.LaunchAgent;

public class LaunchHistory extends AbstractHistory {
	
	public String trigger;
	public ArrayList<OperationHistory> operations;
	
	public LaunchHistory(LaunchAgent launch){

		id = launch.getConfig().getId();
		name = launch.getConfig().getName();
		description = launch.getConfig().getDescription();
		trigger = launch.getTriggerStatus().message;
		start = launch.getStatusManager().getStart();
		end = launch.getStatusManager().getEnd();
		status = launch.getStatusManager().getStatus();
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+start.getTime();		
		artifacts = launch.getArtifacts();
		operations = new ArrayList<OperationHistory>();
		for(AbstractOperation operation : launch.getOperations()){
			operations.add(new OperationHistory(operation));
		}
	}
	
	public void init() throws Exception {
		
		super.init();
		for(OperationHistory entry : operations){
			entry.init();
		}
	}
}
