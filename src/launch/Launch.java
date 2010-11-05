package launch;


public class Launch {

	private LaunchConfig config;
	
	public Launch(LaunchConfig config){
		
		this.config = config;
	}
	
	public LaunchConfig getConfig(){ return config; }

	public static Launch initializeLaunch(LaunchConfig config) {
		
		Launch launch = new Launch(config);
		// TODO
		return launch;
	}
}
