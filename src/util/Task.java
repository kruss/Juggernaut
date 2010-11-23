package util;

import core.Application;

public abstract class Task extends Thread {

	private Application application;
	private long startupDelay;
	private boolean cyclic;
	private long cyclicDelay;
	
	public Task(){
		
		application = Application.getInstance();
		startupDelay = 0;
		cyclic = false;
		cyclicDelay = 0;
	}
	
	public void setCyclic(long delay){ 
		
		this.cyclic = true; 
		this.cyclicDelay = delay;
	}
	
	public void run(){
		
		application.getLogger().debug("Starting Task ["+getName()+"]");
		try{
			Thread.sleep(startupDelay);
			if(cyclic){
				while(cyclic && !isInterrupted()){
					runTask();
					Thread.sleep(cyclicDelay);
				}
			}else{
				runTask();
			}
		}catch(InterruptedException e){ 
			/* NOTHING */ 
		}finally{
			application.getLogger().debug("Stopping Task ["+getName()+"]");
		}
	}
	
	public void start(long delay){
		
		startupDelay = delay;
		start();
	}
	
	public void terminate(){
		
		if(isAlive()){
			interrupt();
			while(isAlive()){ 
				SystemTools.sleep(50);
			}
		}
	}
	
	protected abstract void runTask();
}
