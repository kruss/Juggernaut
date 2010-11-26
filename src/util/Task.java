package util;

public abstract class Task extends Thread {

	protected long startupDelay;
	protected boolean cyclic;
	protected long cyclicDelay;
	protected Logger observer;
	
	public Logger getObserver(){ return observer; }
	
	public Task(Logger observer){
		
		this.observer = observer;
		startupDelay = 0;
		cyclic = false;
		cyclicDelay = 0;
	}
	
	public void setCyclic(long delay){ 
		
		this.cyclic = true; 
		this.cyclicDelay = delay;
	}
	
	public void run(){
		
		observer.debug("Starting Task ["+getName()+"]");
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
			observer.debug("Interrupting Task ["+getName()+"]");
		}finally{
			observer.debug("Stopping Task ["+getName()+"]");
		}
	}
	
	public void asyncRun(long delay){
		
		startupDelay = delay;
		start();
	}
	
	public void syncRun(long delay) throws InterruptedException {
		
		asyncRun(delay);
		join();
	}
	
	public void asyncKill(){
		
		if(isAlive()){
			interrupt();
		}
	}
	
	public void syncKill(){
		
		asyncKill();
		while(isAlive()){ 
			SystemTools.sleep(50);
		}
	}
	
	protected abstract void runTask();
}
