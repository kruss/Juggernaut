package lifecycle;

import java.util.ArrayList;
import java.util.Date;

import trigger.UserTrigger;
import util.StringTools;

import core.Application;
import core.Constants;

public class LaunchManager implements ILifecycleListener {
	
	public static final UserTrigger USER_TRIGGER = new UserTrigger();

	private Application application;
	
	ArrayList<LaunchAgent> agents;
	
	public LaunchManager(){
		
		application = Application.getInstance();
		agents = new ArrayList<LaunchAgent>();
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	public void shutdown() {
		// TODO Auto-generated method stub
	}

	public synchronized LaunchingStatus runLaunch(LaunchAgent launch) {
		
		if(agents.size() < Constants.MAX_AGENTS || launch.getTrigger() == USER_TRIGGER){
			String name = launch.getConfig().getName();
			if(!isLaunchRunning(launch.getConfig().getId())){
				agents.add(launch);
				launch.addListener(this);
				launch.start();
				return new LaunchingStatus("Launch ["+name+"] launched", true);
			}else{
				return new LaunchingStatus("Launch ["+name+"] already running", false);
			}
		}else{
			return new LaunchingStatus("Maximum of "+Constants.MAX_AGENTS+" agents running", false);
		}
	}
	
	private boolean isLaunchRunning(String id) {
		
		for(LaunchAgent agent : agents){
			if(agent.getConfig().getId().equals(id)){
				return true;
			}
		}
		return false;
	}

	public class LaunchingStatus {
		
		public String message;
		public boolean launched;
		
		public LaunchingStatus(String message, boolean launched){
			this.message = message;
			this.launched = launched;
		}
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
}
