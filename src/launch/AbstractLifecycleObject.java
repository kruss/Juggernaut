package launch;

import java.util.ArrayList;
import java.util.Date;

import core.Application;

import launch.ILifecycleListener.Lifecycle;
import launch.StatusManager.Status;
import util.Logger;
import util.Task;

public abstract class AbstractLifecycleObject extends Task {
	
	protected StatusManager statusManager;
	private ArrayList<ILifecycleListener> listeners;
	
	public StatusManager getStatusManager(){ return statusManager; }
	
	public AbstractLifecycleObject(String name){
		super(name, Application.getInstance().getLogger());
		
		statusManager = new StatusManager(this);
		listeners = new ArrayList<ILifecycleListener>();
	}
	
	public void addListener(ILifecycleListener listener){ listeners.add(listener); }
	
	public void notifyListeners(Lifecycle lifecycle){
		for(ILifecycleListener listener : listeners){
			listener.lifecycleChanged(this, lifecycle);
		}
	}
	
	public abstract String getFolder();
	public abstract Logger getLogger();
	
	protected abstract void init() throws Exception;
	protected abstract void execute() throws Exception;
	protected abstract void finish();
	
	public void runTask() {
		
		notifyListeners(Lifecycle.START);
		try{
			statusManager.setStart(new Date());
			init();
			execute();
		}catch(Exception e){
			if(e instanceof InterruptedException){
				getLogger().emph("Interrupted");
				statusManager.setStatus(Status.CANCEL);
			}else{
				getLogger().error(e);
				statusManager.setStatus(Status.FAILURE);
			}
		}finally{
			statusManager.setEnd(new Date());
			finish();
		}
		notifyListeners(Lifecycle.FINISH);
	}
}
