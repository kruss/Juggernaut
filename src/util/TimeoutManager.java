package util;

import java.util.ArrayList;
import java.util.Date;

import core.Application;

public class TimeoutManager extends Task {

	private static final long CYCLE = 10 * 1000; // 10 sec
	
	private ArrayList<TaskMonitor> monitors;
	
	public TimeoutManager(){
		super("TimeoutManager", Application.getInstance().getLogger());
		monitors = new ArrayList<TaskMonitor>();
	}

	public void register(Task task) {
		
		TaskMonitor monitor = new TaskMonitor(task);
		synchronized(monitors){
			monitors.add(monitor);
		}
	}

	public void deregister(Task task) {
		
		synchronized(monitors){
			for(TaskMonitor monitor : monitors){
				if(monitor.task == task){
					monitors.remove(monitor);
					break;
				}
			}
		}
	}
	
	public void init() {
		
		setCycle(CYCLE);
		asyncRun(0, 0);
	}
	
	public void shutdown() {

		synchronized(monitors){
			for(TaskMonitor monitor : monitors){
				monitor.task.syncKill();
			}
		}
		syncKill();
	}

	@Override
	protected void runTask() {

		Date now = new Date();
		synchronized(monitors){
			for(TaskMonitor monitor : monitors){
				if(
						monitor.timeout != null && 
						monitor.timeout.getTime() <= now.getTime()
				){
						monitors.remove(monitor);
						Task task = monitor.task;
						task.getObserver().log("Task Timeout ["+task.getName()+"]");
						task.asyncKill();
				}
			}
		}
	}

	protected class TaskMonitor {
		
		public Task task;
		public Date timeout;
		
		public TaskMonitor(Task task){
			
			this.task = task;
			if(task.getTimeout() > 0){
				timeout = new Date((new Date()).getTime()+task.getTimeout());
			}else{
				timeout = null;
			}
		}
	}
}
