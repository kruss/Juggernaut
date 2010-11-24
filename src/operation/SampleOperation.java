package operation;

import util.StringTools;
import util.SystemTools;
import lifecycle.LaunchAgent;
import lifecycle.StatusManager.Status;
import data.AbstractOperation;

public class SampleOperation extends AbstractOperation {

	private SampleOperationConfig config;
	
	public SampleOperation(LaunchAgent parent, SampleOperationConfig config) {
		super(parent, config);
		this.config = config;
	}

	@Override
	protected void execute() throws Exception {
		
		int idle = config.getIdleTime();
		if(idle > 0){
			logger.log("doing some work...");
			for(int i=1; i<=idle; i++){
				logger.debug("some work ("+i+"/"+idle+")");
				SystemTools.sleep(StringTools.sec2millis(1));
			}
		}else{
			logger.log("nothing todo...");
		}

		if(config.isThrowError()){
			logger.log("throwing an error...");
			statusManager.setStatus(Status.ERROR);
		}
		if(config.isThrowException()){
			logger.log("throwing an exception...");
			throw new Exception("Sample Exception");
		}
	}
}
