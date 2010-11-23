package lifecycle;

import java.io.File;

import core.Application;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.LaunchConfig;
import util.FileTools;
import util.Logger;
import util.Logger.Mode;
import lifecycle.LaunchManager.TriggerStatus;
import lifecycle.StatusManager.Status;

public class LaunchAgent extends AbstractLifecycleObject {

	private LaunchConfig config;
	private TriggerStatus trigger;
	private Logger logger;
	
	public LaunchAgent(LaunchConfig config, TriggerStatus trigger){

		this.config = config.clone();
		this.trigger = trigger;
		setName(config.getName());
	}
	
	public LaunchConfig getConfig(){ return config; }
	public TriggerStatus getTrigger(){ return trigger; }
	
	@Override
	public String getOutputFolder() {
		return Application.getInstance().getOutputFolder()+File.separator+config.getId();
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup launch-folder
		File folder = new File(getOutputFolder());
		if(config.isClean() && folder.isDirectory()){
			FileTools.deleteFolder(folder.getAbsolutePath());
		}
		if(!folder.isDirectory()){
			FileTools.createFolder(folder.getAbsolutePath());
		}
		
		// setup launch-logger
		logger = new Logger(new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE));
		logger.setMode(Mode.FILE_ONLY);
		statusManager.setProgressMax(config.getOperationConfigs().size());
	}
	
	@Override
	protected void execute() throws Exception {
		
		logger.info("Launch ["+config.getName()+"]");
		logger.log("Trigger: "+trigger.message);
		int currentIndex = 1;
		int maxIndex = config.getOperationConfigs().size();
		boolean aboarding = false;
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			
			logger.info(currentIndex+"/"+maxIndex+" Operation ["+operationConfig.getName()+"]");
			AbstractOperation operation = operationConfig.createOperation();
			operation.setParent(this);
			
			if(operationConfig.isActive() && !aboarding){
				
				// start operation
				operation.start();
				operation.join();
				
				// process operation status
				Status operationStatus = operation.getStatusManager().getStatus();
				if(operationStatus == Status.ERROR && operation.getConfig().isCritical()){
					logger.emph("Critical operation failed");
					statusManager.setStatus(Status.FAILURE);
				}else if(operationStatus == Status.FAILURE){
					logger.emph("Operation failed");
					statusManager.setStatus(Status.FAILURE);
				}
				
			}else{
				operation.getStatusManager().setStatus(Status.CANCEL);
			}
			logger.log("Status: "+operation.getStatusManager().getStatus().toString());
			
			// set progress
			statusManager.addProgress(1);
			currentIndex++;
			
			// process launch status
			if(!aboarding && statusManager.getStatus() != Status.PROCESSING){
				logger.emph("Aboarding launch");
				aboarding=true;
			}
		}
	}

	@Override
	protected void finish() {

		// perform output
		// TODO
		
		// perform notification
		// TODO
		
		// final status
		logger.info("Status: "+statusManager.getStatus().toString());
	}
}
