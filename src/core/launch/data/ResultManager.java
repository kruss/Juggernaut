package core.launch.data;

import java.util.ArrayList;

import core.Result;
import core.launch.data.StatusManager.Status;

public class ResultManager {

	public static final String OUTPUT_FILE = "result.xml";

	private StatusManager statusManager;
	private ArrayList<Result> results;
	
	public ResultManager(StatusManager statusManager) {
		
		this.statusManager = statusManager;
		results = new ArrayList<Result>();
	}
	
	public ArrayList<Result> getResults(){ return results; }
	
	public void addResult(Result result){
		
		results.add(result);
		if(result.status == Result.Status.ERROR){
			statusManager.setStatus(Status.ERROR);
		}
	}
	
	public static String getStatusHtml(core.Result.Status status) {
		return "<font color='"+getStatusColor(status)+"'>"+status.toString()+"</font>";
	}
	
	public static String getStatusColor(core.Result.Status status) {

		String color = "black";
		if(status == core.Result.Status.UNDEFINED){
			color = "orange";
		}else if(status == core.Result.Status.SUCCEED){
			color = "green";
		}else if(status == core.Result.Status.ERROR){
			color = "red";
		}else if(status == core.Result.Status.WARNING){
			color = "blue";
		}
		return color;
	}
}
