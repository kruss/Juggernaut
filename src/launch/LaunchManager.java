package launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import launch.StatusManager.Status;
import logger.ILogProvider;

import util.IChangedListener;
import util.StringTools;
import core.Application;

public class LaunchManager implements ILifecycleListener {

	public static TriggerStatus USER_TRIGGER;
	
	private Application application;
	private ArrayList<LaunchAgent> agents;
	private ArrayList<IChangedListener> listeners;
	
	public LaunchManager(){
		
		USER_TRIGGER = new TriggerStatus("Run by user", true);
		
		application = Application.getInstance();
		agents = new ArrayList<LaunchAgent>();
		listeners = new ArrayList<IChangedListener>();
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}

	public void shutdown() {
		for(LaunchAgent agent : agents){
			agent.syncKill();
		}
	}
	
	public synchronized LaunchStatus runLaunch(LaunchAgent launch) {
		
		if(isReady() || launch.getTriggerStatus() == USER_TRIGGER){
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
		return agents.size() < application.getConfig().getMaximumAgents();
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
			application.getWindow().setStatus(
					"Launch ["+agent.getConfig().getName()+"] started at "+date
			);
		}
		if(lifecycle == Lifecycle.FINISH){
			application.getWindow().setStatus(
					"Launch ["+agent.getConfig().getName()+"] finished at "+date
			);
			agents.remove(agent);
		}
		notifyListeners();
	}
	
	public class TriggerStatus {
		
		public String message;
		public boolean triggered;
		
		public TriggerStatus(String message, boolean triggered){
			this.message = message;
			this.triggered = triggered;
		}
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
			trigger = launch.getTriggerStatus().message;
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
