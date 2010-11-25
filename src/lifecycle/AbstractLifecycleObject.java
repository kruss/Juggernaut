package lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import data.Artifact;

import lifecycle.ILifecycleListener.Lifecycle;
import lifecycle.StatusManager.Status;
import util.Logger;
import util.Task;

public abstract class AbstractLifecycleObject extends Task {
	
	protected StatusManager statusManager;
	protected ArrayList<Artifact> artifacts;
	private ArrayList<ILifecycleListener> listeners;
	
	public StatusManager getStatusManager(){ return statusManager; }
	public ArrayList<Artifact> getArtifacts(){ 
		Collections.sort(artifacts);
		return artifacts; 
	}
	
	public AbstractLifecycleObject(){
		
		statusManager = new StatusManager(this);
		artifacts = new ArrayList<Artifact>();
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
			init();
			statusManager.setStart(new Date());
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
