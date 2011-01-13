package util;

import java.util.Date;

import core.runtime.TaskManager;

public abstract class Task extends Thread {

	private long delay;
	private long cycle;
	private long timeout;
	private Date start;

	private TaskManager taskManager;

	public Task(String name, TaskManager taskManager){
		super(name);
		this.taskManager = taskManager;
		
		delay = 0;
		cycle = 0;
		timeout = 0;
		start = null;
	}
	
	public void setCycle(long cycle){ this.cycle = cycle; }
	private boolean isCyclic(){ return cycle > 0; }
	public boolean isExpired(){
		
		if(start != null && timeout > 0){
			if((new Date()).getTime() >= start.getTime()+timeout){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	public void run(){
		
		taskManager.debug("Start\t["+getName()+"]");
		if(taskManager != null){
			taskManager.register(this);
		}
		try{
			Thread.sleep(delay);
			if(isCyclic()){
				runCyclicTask();
			}else{
				runSingleTask();
			}
		}catch(InterruptedException e){ 
			taskManager.debug("Interrupt\t["+getName()+"]");
		}finally{
			taskManager.debug("Stopp\t["+getName()+"]");
			if(taskManager != null){
				taskManager.deregister(this);
			}
		}
	}

	private void runCyclicTask() throws InterruptedException {
		
		while(isCyclic() && !isInterrupted()){
			runSingleTask();
			taskManager.debug("Idle\t["+getName()+"]");
			Thread.sleep(cycle);
		}
	}
	
	private void runSingleTask(){
		
		start = new Date();
		if(taskManager != null){
			taskManager.status(this, true);
		}
		try{
			taskManager.debug("Run\t["+getName()+"]");
			runTask();
		}finally{
			if(taskManager != null){
				taskManager.status(this, false);
			}
			start = null;
		}
	}
	
	protected abstract void runTask();
	
	public void asyncRun(long delay, long timeout){
		
		this.delay = delay;
		this.timeout = timeout;
		start();
	}
	
	public void syncRun(long delay, long timeout) throws InterruptedException {
		
		asyncRun(delay, timeout);
		join();
	}
	
	@SuppressWarnings("deprecation")
	public void asyncKill(final long timeout){
		
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					if(isAlive()){ interrupt(); }
					if(timeout > 0){
						Thread.sleep(timeout);
						if(isAlive()){ stop(); }
					}
				}catch(Exception e){
					taskManager.error(e);
				}
			}
		});
		thread.start();
	}
	
	public void syncKill(long timeout) throws InterruptedException {
		
		asyncKill(timeout);
		join();
	}
}
