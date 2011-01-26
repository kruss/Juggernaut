package core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


import ui.IStatusClient;
import ui.IStatusProvider;
import util.DateTools;
import util.IChangeListener;
import util.IChangeable;
import core.ISystemComponent;
import core.launch.ILifecycleListener;
import core.launch.LaunchAgent;
import core.launch.LifecycleObject;
import core.launch.data.StatusManager.Status;
import core.launch.trigger.AbstractTrigger;
import core.persistence.Configuration;
import core.runtime.logger.ILogProvider;

/** maintains launches to be executed */
public class LaunchManager implements ISystemComponent, ILifecycleListener, IChangeable, IStatusProvider {

	private Configuration configuration;
	private ArrayList<LaunchAgent> agents;
	private ArrayList<IChangeListener> listeners;
	private IStatusClient client;
	
	public LaunchManager(Configuration configuration){
		
		this.configuration = configuration;
		agents = new ArrayList<LaunchAgent>();
		listeners = new ArrayList<IChangeListener>();
		client = null;
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	@Override
	public void setClient(IStatusClient client){ this.client = client; }
	@Override
	public void status(String text){
		if(client != null){
			client.status(text);
		}
	}

	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {
		for(int i=agents.size()-1; i>=0; i--){
			LaunchAgent agent = agents.get(i);
			agent.syncStop(agent.getOperations().size() * 1000);
		}
	}
	
	public LaunchStatus runLaunch(LaunchAgent launch) {
		
		synchronized(agents){
			if(isReady() || launch.getTrigger().equals(AbstractTrigger.USER_TRIGGER)){
				if(!isRunning(launch.getConfig().getId())){
					agents.add(launch);
					launch.addListener(this);
					launch.asyncRun(0, launch.getConfig().getTimeout());
					return new LaunchStatus("Launch started", true);
				}else{
					return new LaunchStatus("Already running", false);
				}
			}else{
				return new LaunchStatus("Maximum tasks", false);
			}
		}
	}
	
	public void stopLaunch(String id) {
		
		LaunchAgent agent = getLaunch(id);
		if(agent != null){ 
			agent.asyncStop(agent.getOperations().size() * 1000); 
		}
	}
	
	public boolean isBusy() {
		
		synchronized(agents){
			return agents.size() > 0;
		}
	}
	
	public boolean isReady() {
		
		synchronized(agents){
			return agents.size() < configuration.getMaximumAgents();
		}
	}
	
	public boolean isRunning(String id) {
		
		LaunchAgent agent = getLaunch(id);
		return agent != null;
	}
	
	private LaunchAgent getLaunch(String id) {
		
		synchronized(agents){
			for(LaunchAgent agent : agents){
				if(agent.getConfig().getId().equals(id)){
					return agent;
				}
			}
			return null;
		}
	}

	@Override
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle) {
		
		LaunchAgent agent = (LaunchAgent)object;
		String date = DateTools.getTextDate(new Date());
		if(lifecycle == Lifecycle.START){
			status("Launch ["+agent.getConfig().getName()+"] has STARTED: "+date);
		}
		if(lifecycle == Lifecycle.FINISH){
			status("Launch ["+agent.getConfig().getName()+"] has FINISHED: "+date);
			synchronized(agents){
				agents.remove(agent);
			}
		}
		notifyListeners();
	}
	
	public class LaunchStatus {
		
		public String message;
		public boolean launched;
		
		public LaunchStatus(String message, boolean launched){
			this.message = message;
			this.launched = launched;
		}
	}
	
	public class LaunchInfo implements Comparable<LaunchInfo> {
		
		public String name;
		public String id;
		public String trigger;
		public Date start;
		public int progress;
		public Status status;
		
		public LaunchInfo(LaunchAgent launch){
			name = launch.getConfig().getName();
			id = launch.getConfig().getId();
			trigger = launch.getTrigger();
			start = launch.getStatusManager().getStart();
			progress = launch.getStatusManager().getProgress();
			status = launch.getStatusManager().getStatus();
		}
		
		@Override
		public int compareTo(LaunchInfo o) {
			return name.compareTo(o.name);
		}
	}
	
	public ArrayList<LaunchInfo> getLaunchInfo(){
		
		synchronized(agents){
			ArrayList<LaunchInfo> infos = new ArrayList<LaunchInfo>();
			for(LaunchAgent agent : agents){
				infos.add(new LaunchInfo(agent));
			}
			Collections.sort(infos);
			return infos;
		}
	}
	
	public ILogProvider getLoggingProvider(String id){
		
		LaunchAgent agent = getLaunch(id);
		if(agent != null){
			return agent.getLogger();
		}else{
			return null;
		}
	}
}
