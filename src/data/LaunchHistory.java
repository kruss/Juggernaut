package data;

import java.io.File;
import java.util.ArrayList;


import core.Application;
import core.Constants;

import launch.LaunchAgent;

public class LaunchHistory extends AbstractHistory {
	
	private transient LaunchAgent launch;
	
	public String logfile;
	public String trigger;
	public ArrayList<OperationHistory> operations;
	
	public LaunchHistory(LaunchAgent launch){

		this.launch = launch;
		
		id = launch.getConfig().getId();
		name = launch.getConfig().getName();
		description = launch.getConfig().getDescription();
		trigger = launch.getTriggerStatus().message;
		operations = new ArrayList<OperationHistory>();		

	}
	
	public void init() throws Exception {
		
		start = launch.getStatusManager().getStart();
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+start.getTime();
		logfile = folder+File.separator+Constants.APP_NAME+".log";
		
		super.init();
		for(OperationHistory entry : operations){
			entry.init();
		}
	}
	
	public void finish() throws Exception {
		
		end = launch.getStatusManager().getEnd();
		status = launch.getStatusManager().getStatus();
		artifacts = launch.getArtifacts();
		
		super.finish();
		for(OperationHistory entry : operations){
			entry.finish();
		}
	}
}
