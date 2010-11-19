package lifecycle;

import java.util.Date;

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
		
		if(setInternalStatus(status)){
			lifecycleObject.notifyListeners();
		}
	}
	private boolean setInternalStatus(Status status){
		
		if(getLevel(this.status) < getLevel(status)){
			this.status = status;
			return true;
		}else{
			return false;
		}
	}
	
	public void setProgressMax(int progress){ progressMax = progress; }
	public void addProgress(int progress){ 
		if(progress > 0 && (this.progress += progress) <= progressMax){
			this.progress += progress; 
			lifecycleObject.notifyListeners();
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
		setInternalStatus(Status.PROCESSING);
		lifecycleObject.notifyListeners();
	}
	public Date getStart(){ return start; }
	public void setEnd(Date end){ 
		this.end = end; 
		if(status == Status.PROCESSING){
			setInternalStatus(Status.SUCCEED);
		}
		lifecycleObject.notifyListeners();
	}
	public Date getEnd(){ return end; }
}
