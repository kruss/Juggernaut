package lifecycle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import core.Application;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.LaunchHistory;
import data.LaunchConfig;
import data.OperationHistory;
import util.FileTools;
import util.Logger;
import util.StringTools;
import util.Logger.Mode;
import lifecycle.LaunchManager.TriggerStatus;
import lifecycle.StatusManager.Status;

public class LaunchAgent extends AbstractLifecycleObject {

	private Application application;
	
	private LaunchConfig config;
	private TriggerStatus triggerStatus;
	protected PropertyManager propertyManager;
	private ArrayList<AbstractOperation> operations;
	private LaunchHistory history;
	private Logger logger;
	
	public LaunchAgent(LaunchConfig config, TriggerStatus trigger){

		this.application = Application.getInstance();
		
		this.config = config.clone();
		this.triggerStatus = trigger;
		
		setName("Launch("+config.getName()+")");
		
		propertyManager = new PropertyManager();
		propertyManager.addProperty(config.getId(), "Name", config.getName());
		propertyManager.addProperty(config.getId(), "Folder", getFolder());
		propertyManager.addProperty(config.getId(), "Trigger", trigger.message);
		propertyManager.addProperty(config.getId(), "Clean", ""+config.isClean());
		propertyManager.addProperty(config.getId(), "Timeout", StringTools.millis2min(config.getTimeout())+" min");
		
		logger = new Logger(Mode.FILE_ONLY);
		
		operations = new ArrayList<AbstractOperation>();
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			AbstractOperation operation = operationConfig.createOperation(this);
			operations.add(operation);
			propertyManager.addProperties(
					operationConfig.getId(), 
					operationConfig.getOptionContainer().getProperties()
			);
		}
		
		history = new LaunchHistory(this);
		for(AbstractOperation operation : operations){
			history.operations.add(new OperationHistory(operation));
		}
		
		statusManager.setProgressMax(operations.size());
	}
	
	public LaunchConfig getConfig(){ return config; }
	public TriggerStatus getTriggerStatus(){ return triggerStatus; }
	public PropertyManager getPropertyManager(){ return propertyManager; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	
	@Override
	public String getFolder() {
		return Application.getInstance().getBuildFolder()+File.separator+config.getId();
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup the history-folder and logger
		history.init();
		logger.setLogiFile(new File(history.logfile));
		
		// setup launch-folder
		File folder = new File(getFolder());
		if(config.isClean() && folder.isDirectory()){
			FileTools.deleteFolder(folder.getAbsolutePath());
		}
		if(!folder.isDirectory()){
			FileTools.createFolder(folder.getAbsolutePath());
		}
	}
	
	@Override
	protected void execute() throws Exception {
		
		logger.info("Launch ["+config.getName()+"]");
		debugProperties(propertyManager.getProperties(config.getId()));
		
		boolean aboarding = false;
		for(AbstractOperation operation : operations){
			try{
				logger.info(
						operation.getIndex()+"/"+config.getOperationConfigs().size()+
						" Operation ["+operation.getConfig().getName()+"]"
				);
				debugProperties(propertyManager.getProperties(operation.getConfig().getId()));
				if(operation.getConfig().isActive() && !aboarding){
					
					// start operation
					operation.syncRun(0);
					
					// process status
					propertyManager.addProperties(
							operation.getConfig().getId(), 
							operation.getStatusManager().getProperties()
					);
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
				logger.log("Operation: "+operation.getStatusManager().getStatus().toString());
				

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

		// process status
		propertyManager.addProperties(
				config.getId(), 
				statusManager.getProperties()
		);
		logger.info("Launch: "+statusManager.getStatus().toString());
		
		
		// perform notification
		// TODO
		
		// perform output
		try{ 
			history.finish();
			application.getHistory().addEntry(history); 
		}catch(Exception e){
			logger.error(e);
		}
		
		logger.clearListeners();
	}
	
	private void debugProperties(HashMap<String, String> properties) {
		
		StringBuilder info = new StringBuilder();
		ArrayList<String> keys = PropertyManager.getKeys(properties);
		for(String key : keys){
			info.append("\t"+key+": "+properties.get(key)+"\n");
		}
		logger.debug("Properties [\n"+info.toString()+"]");
	}
}
