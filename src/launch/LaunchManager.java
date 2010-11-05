package launch;

import core.Application;

public class LaunchManager {

	private static LaunchManager instance;
	
	public static LaunchManager getInstance(){
		if(instance == null){
			instance = new LaunchManager();
		}
		return instance;
	}
	
	private LaunchManager(){}

	public void runLaunch(Launch launch) {
		
		Application.getInstance().getLogger().log("Starting Launch: "+launch.getConfig().getName());
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}

	public void shutdown() {
		// TODO Auto-generated method stub
	}
}
