package operation;

import util.StringTools;
import util.SystemTools;
import launch.LaunchAgent;
import launch.StatusManager.Status;
import logger.Logger.Module;
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
		
		if(config.isThrowError()){
			logger.log(Module.APP, "throwing error...");
			statusManager.setStatus(Status.ERROR);
		}
		if(config.isThrowException()){
			logger.log(Module.APP, "throwing exception...");
			throw new Exception("Sample Exception");
		}
		
		long work = StringTools.millis2sec(config.getIdleTime());
		if(work > 0){
			logger.log(Module.APP, "doing some work...");
			for(long i=1; i<=work; i++){
				logger.debug(Module.APP, "work ("+i+"/"+work+")");
				SystemTools.sleep(StringTools.sec2millis(1));
			}
		}
		logger.log(Module.APP, "done.");
	}
}
