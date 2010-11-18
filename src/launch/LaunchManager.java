package launch;

import core.Application;

public class LaunchManager {
	
	private Application application;
	
	public LaunchManager(){
		
		application = Application.getInstance();
	}

	public void runLaunch(Launch launch) {
		
		application.getLogger().log("Starting Launch: "+launch.getConfig().getName());
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}

	public void shutdown() {
		// TODO Auto-generated method stub
	}
}
