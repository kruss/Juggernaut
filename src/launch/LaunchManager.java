package launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import launch.StatusManager.Status;
import logger.ILogProvider;

import ui.IStatusClient;
import util.IChangedListener;
import util.StringTools;
import core.Configuration;
import core.ISystemComponent;
import data.AbstractTrigger;

/** maintains launches to be executed */
public class LaunchManager implements ISystemComponent, ILifecycleListener {

	private Configuration configuration;
	private ArrayList<LaunchAgent> agents;
	private ArrayList<IChangedListener> listeners;
	private ArrayList<IStatusClient> clients;
	
	public LaunchManager(Configuration configuration){
		
		this.configuration = configuration;
		agents = new ArrayList<LaunchAgent>();
		listeners = new ArrayList<IChangedListener>();
		clients = new ArrayList<IStatusClient>();
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public void addClient(IStatusClient client){ clients.add(client); }
	
	public void setStatus(String text){
		for(IStatusClient client : clients){
			client.status(text);
		}
	}

	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {
		for(LaunchAgent agent : agents){
			agent.syncKill();
		}
	}
	
	public synchronized LaunchStatus runLaunch(LaunchAgent launch) {
		
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
	
	public void stopLaunch(String id) {
		
		LaunchAgent agent = getAgent(id);
		if(agent != null){ agent.asyncKill(); }
	}
	
	public synchronized boolean isBusy() {
		return agents.size() > 0;
	}
	
	public synchronized boolean isReady() {
		return agents.size() < configuration.getMaximumAgents();
	}
	
	private synchronized boolean isRunning(String id) {
		
		LaunchAgent agent = getAgent(id);
		return agent != null;
	}
	
	private synchronized LaunchAgent getAgent(String id) {
		
		for(LaunchAgent agent : agents){
			if(agent.getConfig().getId().equals(id)){
				return agent;
			}
		}
		return null;
	}

	@Override
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle) {
		
		LaunchAgent agent = (LaunchAgent)object;
		String date = StringTools.getTextDate(new Date());
		if(lifecycle == Lifecycle.START){
			setStatus("Launch ["+agent.getConfig().getName()+"] started at "+date);
		}
		if(lifecycle == Lifecycle.FINISH){
			setStatus("Launch ["+agent.getConfig().getName()+"] finished at "+date);
			agents.remove(agent);
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
		
		ArrayList<LaunchInfo> infos = new ArrayList<LaunchInfo>();
		for(LaunchAgent agent : agents){
			infos.add(new LaunchInfo(agent));
		}
		Collections.sort(infos);
		return infos;
	}
	
	public ILogProvider getLoggingProvider(String id){
		
		LaunchAgent agent = getAgent(id);
		if(agent != null){
			return agent.getLogger();
		}else{
			return null;
		}
	}
}
