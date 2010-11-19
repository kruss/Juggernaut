package lifecycle;

import java.util.ArrayList;
import java.util.Date;

import lifecycle.StatusManager.Status;
import util.IChangedListener;
import util.Logger;


public abstract class AbstractLifecycleObject extends Thread {

	protected StatusManager statusManager;
	protected ArtifactManager artifactManager;
	protected PropertyManager propertyManager;
	
	private ArrayList<IChangedListener> listeners;
	
	public StatusManager getStatusManager(){ return statusManager; }
	public ArtifactManager getArtifactManager(){ return artifactManager; }
	public PropertyManager getPropertyManager(){ return propertyManager; }
	
	public AbstractLifecycleObject(){
		
		statusManager = new StatusManager(this);
		artifactManager = new ArtifactManager(this);
		propertyManager = new PropertyManager(this);
		
		listeners = new ArrayList<IChangedListener>();
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public abstract String getOutputFolder();
	public abstract Logger getLogger();
	
	protected abstract void init() throws Exception;
	protected abstract void execute() throws Exception;
	protected abstract void finish();
	
	public void run() {
		
		try{
			init();
			statusManager.setStart(new Date());
			execute();
		}catch(Exception e){
			getLogger().error(e);
			statusManager.setStatus(Status.FAILURE);
		}finally{
			statusManager.setEnd(new Date());
			finish();
		}
	}
}
