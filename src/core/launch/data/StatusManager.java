package core.launch.data;

import java.util.Date;

import core.launch.LifecycleObject;
import core.launch.ILifecycleListener.Lifecycle;


public class StatusManager {

	public enum Status {
		UNDEFINED, PROCESSING, SUCCEED, ERROR, FAILURE, CANCEL
	}
	
	private LifecycleObject parent;
	private Status status;
	private int progress;
	private int progressMax;
	private Date start;
	private Date end;
	
	public StatusManager(LifecycleObject parent){
		
		this.parent = parent;
		status = Status.UNDEFINED;
		progress = 0;
		progressMax = 0;
		start = null;
		end = null;
	}
	
	public long getHash(){
		return (parent.getId()+status.toString()).hashCode();
	}
	
	public Status getStatus(){ return status; }
	public void setStatus(Status status){
		
		if(getStatusValue(this.status) < getStatusValue(status)){
			this.status = status;
			parent.notifyListeners(Lifecycle.PROCESSING);
		}
	}
	
	public void setProgressMax(int progress){ progressMax = progress; }
	public void addProgress(int progress){ 
		
		if(progress > 0 && (this.progress + progress) <= progressMax){
			this.progress += progress; 
			parent.notifyListeners(Lifecycle.PROCESSING);
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

	public static int getStatusValue(Status status){
		
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
	
	public static String getStatusColor(Status status) {

		String color = "black";
		if(status == Status.UNDEFINED){
			color = "orange";
		}else if(status == Status.SUCCEED){
			color = "green";
		}else if(status == Status.ERROR){
			color = "red";
		}else if(status == Status.FAILURE){
			color = "purple";
		}
		return color;
	}
	
	public static String getStatusHtml(Status status) {
		
		return "<font color='"+getStatusColor(status)+"'>"+status.toString()+"</font>";
	}
}
