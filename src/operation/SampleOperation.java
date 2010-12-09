package operation;

import util.StringTools;
import util.SystemTools;
import launch.LaunchAgent;
import launch.StatusManager.Status;
import data.AbstractOperation;

public class SampleOperation extends AbstractOperation {

	private SampleOperationConfig config;
	
	public SampleOperation(LaunchAgent parent, SampleOperationConfig config) {
		super(parent, config);
		this.config = config;
	}

	@Override
	public String getDescription() {
		return 
			"Idle: "+StringTools.millis2sec(config.getIdleTime())+" sec" +
			(config.isThrowError() ? " / Throwing: error" : "") +
			(config.isThrowException() ? " / Throwing: exception" : "") + 
			(config.isThrowError() && config.isThrowException() ? " / Throwing: error & exception" : "");
	}
	
	@Override
	protected void execute() throws Exception {
		
		long sec = StringTools.millis2sec(config.getIdleTime());
		if(sec > 0){
			logger.log("doing some work...");
			for(long i=1; i<=sec; i++){
				logger.debug("some work ("+i+"/"+sec+")");
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
