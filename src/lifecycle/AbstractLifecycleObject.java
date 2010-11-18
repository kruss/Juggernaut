package lifecycle;

import java.util.ArrayList;
import java.util.Date;

import lifecycle.StatusManager.Status;
import util.IChangeListener;
import util.Logger;


public abstract class AbstractLifecycleObject extends Thread {

	protected StatusManager statusManager;
	protected ArtifactManager artifactManager;
	protected PropertyManager propertyManager;
	
	private ArrayList<IChangeListener> listeners;
	
	public StatusManager getStatusManager(){ return statusManager; }
	public ArtifactManager getArtifactManager(){ return artifactManager; }
	public PropertyManager getPropertyManager(){ return propertyManager; }
	
	public AbstractLifecycleObject(){
		
		statusManager = new StatusManager(this);
		artifactManager = new ArtifactManager(this);
		propertyManager = new PropertyManager(this);
		
		listeners = new ArrayList<IChangeListener>();
	}
	
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public abstract String getOutputFolder();
	public abstract Logger getLogger();
	
	protected abstract void init() throws Exception;
	protected abstract void execute() throws Exception;
	protected abstract void finish() throws Exception;
	
	public void run() {
		
		try{
			statusManager.setStart(new Date());
			init();
			execute();
			finish();
		}catch(Exception e){
			getLogger().error(e);
			statusManager.setStatus(Status.FAILURE);
		}finally{
			statusManager.setEnd(new Date());
		}
	}
}
