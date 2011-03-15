package core.launch.operation;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.runtime.TaskManager;
import core.runtime.logger.ILogConfig.Module;
import util.DateTools;
import util.SystemTools;


public class SampleOperation extends AbstractOperation {

	private SampleOperationConfig config;
	
	public SampleOperation(Cache cache, TaskManager taskManager, LaunchAgent parent, SampleOperationConfig config) {
		super(cache, taskManager, parent, config);
		this.config = (SampleOperationConfig) super.config;
	}

	@Override
	public String getRuntimeDescription() {
		return 
			"Idle: "+DateTools.millis2sec(config.getIdleTime())+" sec" +
			(config.isThrowError() ? " / Throwing: error" : "") +
			(config.isThrowException() ? " / Throwing: exception" : "") + 
			(config.isThrowError() && config.isThrowException() ? " / Throwing: error & exception" : "");
	}
	
	@Override
	protected void execute() throws Exception {
		
		if(config.isThrowError()){
			logger.log(Module.COMMON, "throwing error...");
			statusManager.addError(this, null, "Sample Error");
		}
		if(config.isThrowException()){
			logger.log(Module.COMMON, "throwing exception...");
			throw new Exception("Sample Exception");
		}
		
		long work = DateTools.millis2sec(config.getIdleTime());
		if(work > 0){
			logger.log(Module.COMMON, "doing some work...");
			for(long i=1; i<=work; i++){
				logger.debug(Module.COMMON, "work ("+i+"/"+work+")");
				SystemTools.sleep(DateTools.sec2millis(1));
			}
		}
		logger.log(Module.COMMON, "done.");
	}
}
