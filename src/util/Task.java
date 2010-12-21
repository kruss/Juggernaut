package util;

import java.util.Date;

import logger.Logger;
import logger.Logger.Module;


import core.Application;
import core.TaskManager;

public abstract class Task extends Thread {

	private TaskManager manager;
	protected Logger observer;
	private long delay;
	private long cycle;
	private long timeout;
	private Date start;

	public Task(String name, Logger logger){
		
		super(name);
		manager = Application.getInstance().getTaskManager();
		observer = logger;
		delay = 0;
		cycle = 0;
		timeout = 0;
		start = null;
	}
	
	public Logger getObserver(){ return observer; }
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
		
		observer.debug(Module.TASK, "Start ["+getName()+"]");
		try{
			Thread.sleep(delay);
			if(isCyclic()){
				runCyclicTask();
			}else{
				runSingleTask();
			}
		}catch(InterruptedException e){ 
			observer.debug(Module.TASK, "Interrupt ["+getName()+"]");
		}finally{
			observer.debug(Module.TASK, "Stopp ["+getName()+"]");
		}
	}

	private void runCyclicTask() throws InterruptedException {
		
		while(isCyclic() && !isInterrupted()){
			runSingleTask();
			observer.debug(Module.TASK, "Idle ["+getName()+"]");
			Thread.sleep(cycle);
		}
	}
	
	private void runSingleTask(){
		
		start = new Date();
		if(manager != null){
			manager.register(this);
		}
		try{
			observer.debug(Module.TASK, "Run ["+getName()+"]");
			runTask();
		}finally{
			if(manager != null){
				manager.deregister(this);
			}
			start = null;
		}
	}
	
	protected abstract void runTask();
	
	public void syncRun(long delay, long timeout) throws InterruptedException {
		
		asyncRun(delay, timeout);
		join();
	}
	
	public void asyncRun(long delay, long timeout){
		
		this.delay = delay;
		this.timeout = timeout;
		start();
	}
	
	public void syncKill(){
		
		asyncKill();
		while(isAlive()){ 
			SystemTools.sleep(50);
		}
	}
	
	public void asyncKill(){
		
		if(isAlive()){
			interrupt();
		}
	}
}
