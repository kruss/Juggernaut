package lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import lifecycle.StatusManager.Status;

import util.IChangedListener;
import util.ILoggingProvider;
import util.StringTools;
import core.Application;
import core.Configuration;

public class LaunchManager implements ILifecycleListener {

	public static TriggerStatus USER_TRIGGER;
	public static TriggerStatus INITIAL_TRIGGER;
	
	private Application application;
	private SchedulerTask scheduler;
	private ArrayList<LaunchAgent> agents;
	private ArrayList<IChangedListener> listeners;
	private boolean active;
	
	public SchedulerTask getScheduler(){ return scheduler; }
	
	public LaunchManager(){
		
		USER_TRIGGER = new TriggerStatus("Run by user", true);
		INITIAL_TRIGGER = new TriggerStatus("Initial run", true);
		
		application = Application.getInstance();
		scheduler = null;
		agents = new ArrayList<LaunchAgent>();
		listeners = new ArrayList<IChangedListener>();
		active = false;
	}
	
	public void init() {
		
		Configuration configuration = application.getConfiguration();
		if(configuration.isScheduler()){
			startScheduler(configuration.getSchedulerIntervall());
		}
		active = true;
	}
	
	public void shutdown() {
		
		active = false;
		stopScheduler();
		for(LaunchAgent agent : agents){
			agent.syncKill();
		}
	}
	
	public synchronized void startScheduler(long delay){ 
		if(scheduler == null){
			scheduler = new SchedulerTask();
			scheduler.asyncRun(delay); 
		}
	}
	
	public synchronized void stopScheduler(){ 
		if(scheduler != null){
			scheduler.syncKill();
			scheduler = null;
		}
	}

	public synchronized LaunchStatus runLaunch(LaunchAgent launch) {
		
		if(isReady() || launch.getTriggerStatus() == USER_TRIGGER){
			if(!isRunning(launch.getConfig().getId())){
				agents.add(launch);
				launch.addListener(this);
				launch.asyncRun(0);
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
		return agents.size() < application.getConfiguration().getMaximumAgents();
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
	public void lifecycleChanged(AbstractLifecycleObject object, Lifecycle lifecycle) {
		
		if(active){
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
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
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
	
	public ILoggingProvider getLoggingProvider(String id){
		
		LaunchAgent agent = getAgent(id);
		if(agent != null){
			return agent.getLogger();
		}else{
			return null;
		}
	}
}
