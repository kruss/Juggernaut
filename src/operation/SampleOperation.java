package operation;

import util.SystemTools;
import lifecycle.StatusManager.Status;
import data.AbstractOperation;

public class SampleOperation extends AbstractOperation {

	private SampleOperationConfig config;
	
	public SampleOperation(SampleOperationConfig config) {
		super(config);
		this.config = config;
	}

	@Override
	protected void execute() throws Exception {
		
		int idle = config.getIdleTime();
		if(idle > 0){
			for(int i=1; i<=idle; i++){
				logger.log("staying idle... ("+i+"/"+idle+")");
				SystemTools.sleep(1000);
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
