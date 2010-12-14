package launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import core.Application;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.LaunchHistory;
import data.LaunchConfig;
import data.OperationHistory;
import util.FileTools;
import util.StringTools;
import util.SystemTools;
import launch.LaunchManager.TriggerStatus;
import launch.StatusManager.Status;
import logger.Logger;
import logger.Logger.Mode;
import logger.Logger.Module;

public class LaunchAgent extends AbstractLifecycleObject {

	private Application application;
	
	private LaunchConfig config;
	private TriggerStatus triggerStatus;
	protected PropertyContainer propertyContainer;
	private ArrayList<AbstractOperation> operations;
	private LaunchHistory history;
	private Logger logger;
	private boolean aboard;
	
	public LaunchAgent(LaunchConfig config, TriggerStatus trigger){

		super("Launch("+config.getId()+")");
		this.application = Application.getInstance();
		
		this.config = config.clone();
		triggerStatus = trigger;
		logger = new Logger(Mode.FILE);
		
		propertyContainer = new PropertyContainer();
		propertyContainer.addProperty(config.getId(), "Name", config.getName());
		propertyContainer.addProperty(config.getId(), "Folder", getFolder());
		propertyContainer.addProperty(config.getId(), "Trigger", trigger.message);
		propertyContainer.addProperty(config.getId(), "Clean", ""+config.isClean());
		propertyContainer.addProperty(config.getId(), "Timeout", StringTools.millis2min(config.getTimeout())+" min");
		
		operations = new ArrayList<AbstractOperation>();
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			if(operationConfig.isActive()){
				AbstractOperation operation = operationConfig.createOperation(this);
				operations.add(operation);
				propertyContainer.addProperties(
						operationConfig.getId(), 
						operationConfig.getOptionContainer().getProperties()
				);
			}
		}
		
		history = new LaunchHistory(this);
		for(AbstractOperation operation : operations){
			history.operations.add(new OperationHistory(operation));
		}
		
		statusManager.setProgressMax(operations.size());
		aboard = false;
	}
	
	public LaunchConfig getConfig(){ return config; }
	public TriggerStatus getTriggerStatus(){ return triggerStatus; }
	public PropertyContainer getPropertyContainer(){ return propertyContainer; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	
	@Override
	public String getFolder() {
		return 
			Application.getInstance().getFileManager().getBuildFolderPath()+
			File.separator+config.getId();
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup the history-folder
		history.init();
		
		// setup the logger
		logger.setLogfile(new File(history.logfile), 0);
		logger.info(Module.APP, "Launch ["+config.getName()+"]");
		debugProperties(propertyContainer.getProperties(config.getId()));
		
		// setup launch-folder
		File folder = new File(getFolder());
		if(config.isClean() && folder.isDirectory()){
			logger.log(Module.APP, "cleaning: "+folder.getAbsolutePath());
			FileTools.deleteFolder(folder.getAbsolutePath());
		}
		if(!folder.isDirectory()){
			FileTools.createFolder(folder.getAbsolutePath());
		}
	}
	
	@Override
	protected void execute() throws Exception {
		
		for(AbstractOperation operation : operations){
			try{
				Status operationStatus = executeOperation(operation);
				logger.log(Module.APP, "Operation: "+operationStatus.toString());	
				if(operationStatus == Status.ERROR){
					if(!operation.getConfig().isCritical()){
						statusManager.setStatus(Status.ERROR);
					}else{
						logger.emph(Module.APP, "Critical operation failed");
						statusManager.setStatus(Status.FAILURE);
						aboard = true;
					}
				}else if(operationStatus == Status.FAILURE){
					statusManager.setStatus(Status.FAILURE);
					aboard = true;
				}
			}catch(InterruptedException e){
				logger.emph(Module.APP, "Interrupted");
				statusManager.setStatus(Status.CANCEL);
				aboard = true;
				if(operation.isAlive()){
					operation.syncKill();
					operation.getStatusManager().setStatus(Status.CANCEL);
				}
			}finally{
				statusManager.addProgress(1);
			}
		}
	}

	private Status executeOperation(AbstractOperation operation)throws Exception {
		
		logger.info(
				Module.APP, 
				operation.getIndex()+"/"+config.getOperationConfigs().size()+
				" Operation ["+operation.getConfig().getName()+"]"
		);
		debugProperties(propertyContainer.getProperties(operation.getConfig().getId()));
		if(!aboard){
			operation.syncRun(0, 0);
			propertyContainer.addProperties(
					operation.getConfig().getId(), 
					operation.getStatusManager().getProperties()
			);
		}else{
			operation.getStatusManager().setStatus(Status.CANCEL);
		}
		return operation.getStatusManager().getStatus();
	}

	@Override
	protected void finish() {

		propertyContainer.addProperties(
				config.getId(), 
				statusManager.getProperties()
		);
		
		//TODO perform notification
		
		try{ 
			history.finish();
			application.getHistory().addEntry(history); 
		}catch(Exception e){
			logger.error(Module.APP, e);
		}
		
		if(triggerStatus == LaunchManager.USER_TRIGGER){
			try{
				String path = history.getIndexPath();
				logger.debug(Module.APP, "browser: "+path);
				SystemTools.openBrowser(path);
			}catch(IOException e) {
				logger.error(Module.APP, e);
			}
		}
		
		logger.info(Module.APP, "Launch: "+statusManager.getStatus().toString());
		logger.clearListeners();
	}
	
	private void debugProperties(HashMap<String, String> properties) {
		
		StringBuilder info = new StringBuilder();
		ArrayList<String> keys = PropertyContainer.getKeys(properties);
		for(String key : keys){
			info.append("\t"+key+": "+properties.get(key)+"\n");
		}
		logger.debug(Module.APP, "Properties [\n"+info.toString()+"]");
	}
}
