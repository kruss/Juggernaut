package util;

public abstract class Task extends Thread {

	private boolean cyclic;
	private long delay;
	
	public Task(){
		cyclic = false;
		delay = 0;
	}
	
	public void setCyclic(long delay){ 
		this.cyclic = true; 
		this.delay = delay;
	}
	
	public void run(){
		
		try{
			if(cyclic){
				while(cyclic && !isInterrupted()){
					Thread.sleep(delay);
					runTask();
				}
			}else{
				runTask();
			}
		}catch(InterruptedException e){ /* NOTHING */ }
	}
	
	public void terminate(){
		
		cyclic = false;
		interrupt();
	}
	
	protected abstract void runTask();
}
