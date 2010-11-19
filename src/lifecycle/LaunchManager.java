package lifecycle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import util.StringTools;
import core.Application;
import core.Constants;

public class LaunchManager implements ILifecycleListener {

	public static TriggerStatus USER_TRIGGER;
	public static TriggerStatus INITIAL_TRIGGER;
	
	private Application application;
	private ScheduleTask scheduler;
	private HashMap<String, String> cache;
	private ArrayList<LaunchAgent> agents;
	
	public HashMap<String, String> getCache(){ return cache; }
	
	public LaunchManager(){
		
		USER_TRIGGER = new TriggerStatus("Run by user", true);
		INITIAL_TRIGGER = new TriggerStatus("Initial run", true);
		
		application = Application.getInstance();
		scheduler = new ScheduleTask();
		cache = new HashMap<String, String>();
		agents = new ArrayList<LaunchAgent>();
	}
	
	public void init() {
		
		scheduler.start();
	}
	
	public void shutdown() {
		
		scheduler.terminate();
		for(LaunchAgent agent : agents){
			agent.terminate();
		}
	}

	public synchronized LaunchStatus runLaunch(LaunchAgent launch) {
		
		if(isReady() || launch.getTrigger() == USER_TRIGGER){
			if(!isRunning(launch)){
				agents.add(launch);
				launch.addListener(this);
				launch.start();
				return new LaunchStatus("Launch started", true);
			}else{
				return new LaunchStatus("Already running", false);
			}
		}else{
			return new LaunchStatus("Maximum tasks", false);
		}
	}
	
	public synchronized boolean isBusy() {
		return agents.size() > 0;
	}
	
	public synchronized boolean isReady() {
		return agents.size() < Constants.MAX_AGENTS;
	}
	
	private boolean isRunning(LaunchAgent launch) {
		
		for(LaunchAgent agent : agents){
			if(agent.getConfig().getId().equals(launch.getConfig().getId())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void lifecycleChanged(AbstractLifecycleObject object, Lifecycle lifecycle) {
		
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
			agents.remove(object);
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
}
