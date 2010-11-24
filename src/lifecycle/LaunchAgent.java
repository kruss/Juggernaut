package lifecycle;

import java.io.File;

import core.Application;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.LaunchConfig;
import util.FileTools;
import util.Logger;
import util.StringTools;
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
		setName("Launch("+config.getName()+")");
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
		logger = new Logger(Mode.FILE_ONLY);
		logger.setLogiFile(new File(getOutputFolder()+File.separator+Logger.OUTPUT_FILE));
		statusManager.setProgressMax(config.getOperationConfigs().size());
	}
	
	@Override
	protected void execute() throws Exception {
		
		logger.info("Launch ["+config.getName()+"]");
		logger.log("Trigger: "+trigger.message);
		logger.log("Clean: "+config.isClean());
		logger.log("Timeout: "+StringTools.millis2min(config.getTimeout())+" min");
		
		boolean aboarding = false;
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			
			AbstractOperation operation = operationConfig.createOperation(this);
			try{
				logger.info(
						operation.getIndex()+"/"+config.getOperationConfigs().size()+
						" Operation ["+operationConfig.getName()+"]"
				);
				logger.debug("Options:\n"+operationConfig.getOptionContainer().toString());
				
				if(operationConfig.isActive() && !aboarding){
					
					// start operation
					operation.syncRun(0);
					
					// process status
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
				

			}catch(InterruptedException e){
				logger.emph("Interrupted");
				operation.getStatusManager().setStatus(Status.CANCEL);
				operation.syncKill();
				statusManager.setStatus(Status.CANCEL);
			}finally{
				
				// process progress
				statusManager.addProgress(1);
				if(!aboarding && statusManager.getStatus() != Status.PROCESSING){
					logger.emph("Aboarding launch");
					aboarding = true;
				}
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
		logger.clearListeners();
	}
}
