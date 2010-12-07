package util;

import core.Application;

public abstract class Task extends Thread {

	protected Logger observer;
	private long delay;
	private long cycle;
	private long timeout;

	public Task(String name, Logger logger){
		
		setName(name);
		observer = logger;
		delay = 0;
		cycle = 0;
		timeout = 0;
	}
	
	public Logger getObserver(){ return observer; }
	public void setCycle(long cycle){ this.cycle = cycle; }
	public long getTimeout(){ return timeout; }
	
	public void run(){
		
		observer.debug("Starting Task ["+getName()+"]");
		try{
			Thread.sleep(delay);
			if(isCyclic()){
				runCyclicTask();
			}else{
				runSingleTask();
			}
		}catch(InterruptedException e){ 
			observer.debug("Interrupting Task ["+getName()+"]");
		}finally{
			observer.debug("Stopping Task ["+getName()+"]");
		}
	}
	
	private boolean isCyclic(){ return cycle > 0; }

	private void runCyclicTask() throws InterruptedException {
		
		while(isCyclic() && !isInterrupted()){
			runSingleTask();
			Thread.sleep(cycle);
		}
	}
	
	private void runSingleTask(){
		
		TimeoutManager timeoutManager = Application.getInstance().getTimeoutManager();
		try{
			timeoutManager.register(this);
			runTask();
		}finally{
			timeoutManager.deregister(this);
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
