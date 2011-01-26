package core.launch;

import java.util.ArrayList;
import java.util.Date;

import core.html.AbstractHtmlPage;
import core.launch.ILifecycleListener.Lifecycle;
import core.launch.data.Artifact;
import core.launch.data.StatusManager;
import core.launch.data.StatusManager.Status;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;


import ui.option.OptionContainer;
import util.Task;

public abstract class LifecycleObject extends Task {
	
	protected StatusManager statusManager;
	protected ArrayList<Artifact> artifacts;
	private ArrayList<ILifecycleListener> listeners;
	protected String historyFolder;
	
	public StatusManager getStatusManager(){ return statusManager; }
	public ArrayList<Artifact> getArtifacts(){ return artifacts; }
	public void setHistoryFolder(String folder){ this.historyFolder = folder; }
	
	public LifecycleObject(String name, TaskManager taskManager){
		super(name, taskManager);
		statusManager = new StatusManager(this);
		artifacts = new ArrayList<Artifact>();
		listeners = new ArrayList<ILifecycleListener>();
		historyFolder = null;
	}
	
	public void addListener(ILifecycleListener listener){ listeners.add(listener); }
	
	public void notifyListeners(Lifecycle lifecycle){
		for(ILifecycleListener listener : listeners){
			listener.lifecycleChanged(this, lifecycle);
		}
	}
	
	public abstract String getId();
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
				getLogger().emph(Module.COMMON, "Interrupted");
				statusManager.setStatus(Status.CANCEL);
			}else{
				getLogger().error(Module.COMMON, e);
				statusManager.setStatus(Status.FAILURE);
			}
		}finally{
			statusManager.setEnd(new Date());
			finish();
			notifyListeners(Lifecycle.FINISH);
		}
	}
	
	protected class ConfigPage extends AbstractHtmlPage {

		private OptionContainer container;
		
		public ConfigPage(String name, OptionContainer container) {
			super("Config ["+name+"]", null, null);
			this.container = container;
		}
		
		@Override
		public String getBody() {
			return container.toHtml();
		}
	}
}
