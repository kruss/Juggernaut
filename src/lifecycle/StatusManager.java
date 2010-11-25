package lifecycle;

import java.util.Date;
import java.util.HashMap;

import util.StringTools;

import lifecycle.ILifecycleListener.Lifecycle;

public class StatusManager {

	public enum Status {
		UNDEFINED, PROCESSING, SUCCEED, ERROR, FAILURE, CANCEL
	}
	
	private AbstractLifecycleObject lifecycleObject;
	private Status status;
	private int progress;
	private int progressMax;
	private Date start;
	private Date end;
	
	public StatusManager(AbstractLifecycleObject lifecycleObject){
		
		this.lifecycleObject = lifecycleObject;
		status = Status.UNDEFINED;
		progress = 0;
		progressMax = 0;
		start = null;
		end = null;
	}
	
	private int getLevel(Status status){
		
		if(status == Status.UNDEFINED){
			return 0;
		}else if(status == Status.PROCESSING){
			return 1;
		}else if(status == Status.SUCCEED){
			return 2;
		}else if(status == Status.ERROR){
			return 3;
		}else if(status == Status.FAILURE){
			return 4;
		}else if(status == Status.CANCEL){
			return 4;
		}else{
			return -1;
		}
	}
	
	public Status getStatus(){ return status; }
	public void setStatus(Status status){
		
		if(getLevel(this.status) < getLevel(status)){
			this.status = status;
			lifecycleObject.notifyListeners(Lifecycle.PROCESSING);
		}
	}
	
	public void setProgressMax(int progress){ progressMax = progress; }
	public void addProgress(int progress){ 
		
		if(progress > 0 && (this.progress + progress) <= progressMax){
			this.progress += progress; 
			lifecycleObject.notifyListeners(Lifecycle.PROCESSING);
		}
	}
	public int getProgress(){ 
		
		if(progress > 0 && progress <= progressMax){
			return (int)Math.round(((double)progress / (double)progressMax) * 100);
		}else{
			return 0;
		}
	}
	
	public void setStart(Date start){ 
		this.start = start; 
		setStatus(Status.PROCESSING);
	}
	public Date getStart(){ return start; }
	public void setEnd(Date end){ 
		this.end = end; 
		if(status == Status.PROCESSING){
			setStatus(Status.SUCCEED);
		}
	}
	public Date getEnd(){ return end; }
	
	public HashMap<String, String> getProperties(){
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Status", status.toString());
		if(start != null){
			map.put("Start", StringTools.getTextDate(start));
		}
		if(end != null){
			map.put("End", StringTools.getTextDate(end));
		}
		if(start != null &&end != null){
			map.put("Time", StringTools.getTimeDiff(start, end)+" min");
		}
		return map;
	}
}
