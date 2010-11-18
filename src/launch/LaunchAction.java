package launch;

import java.io.File;

import operation.AbstractOperation;
import operation.AbstractOperationConfig;

import core.Application;
import util.Logger;
import lifecycle.AbstractLifecycleObject;
import lifecycle.StatusManager.Status;


public class LaunchAction extends AbstractLifecycleObject {

	private LaunchConfig config;
	private Logger logger;
	
	public LaunchAction(LaunchConfig config){
		
		this.config = config.clone();
	}
	
	public LaunchConfig getConfig(){ return config; }
	
	@Override
	public String getOutputFolder() {
		return Application.getInstance().getOutputFolder()+File.separator+config.getId();
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		File folder = new File(getOutputFolder());
		if(!folder.isDirectory()){
			folder.mkdirs();
		}
		logger = new Logger(new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE));
	}
	
	@Override
	protected void execute() throws Exception {
		
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			AbstractOperation operation = operationConfig.createOperation();
			operation.setParent(this);
			operation.start();
			operation.join();
			getStatusManager().setStatus(operation.getStatusManager().getStatus());
			if(
					getStatusManager().getStatus() == Status.FAILURE ||
					getStatusManager().getStatus() == Status.CANCEL
			){
				break;
			}
		}
	}

	@Override
	protected void finish() throws Exception {
		// TODO Auto-generated method stub
	}
}
