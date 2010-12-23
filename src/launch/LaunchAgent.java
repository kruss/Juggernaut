package launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import core.Configuration;
import core.FileManager;
import core.History;
import core.TaskManager;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.AbstractTrigger;
import data.Artifact;
import data.LaunchHistory;
import data.LaunchConfig;
import data.OperationHistory;
import util.FileTools;
import util.StringTools;
import util.SystemTools;
import launch.StatusManager.Status;
import logger.Logger;
import logger.Logger.Mode;
import logger.Logger.Module;

public class LaunchAgent extends LifecycleObject {

	private History history;
	private FileManager fileManager;
	private LaunchConfig config;
	private String trigger;
	private PropertyContainer propertyContainer;
	private ArrayList<AbstractOperation> operations;
	private LaunchHistory launchHistory;
	private Logger logger;
	private boolean aboard;
	
	public LaunchAgent(
			Configuration configuration, 
			History history, 
			FileManager fileManager, 
			TaskManager taskManager, 
			LaunchConfig config, 
			String trigger)
	{
		super("Launch("+config.getId()+")", taskManager);
		
		this.fileManager = fileManager;
		this.history = history;
		this.config = config.clone();
		this.trigger = trigger;
		logger = new Logger(Mode.FILE);
		logger.setLogManager(configuration);
		
		propertyContainer = new PropertyContainer();
		propertyContainer.addProperty(config.getId(), "Name", config.getName());
		propertyContainer.addProperty(config.getId(), "Folder", getFolder());
		propertyContainer.addProperty(config.getId(), "Trigger", trigger);
		propertyContainer.addProperty(config.getId(), "Clean", ""+config.isClean());
		propertyContainer.addProperty(config.getId(), "Timeout", StringTools.millis2min(config.getTimeout())+" min");
		
		operations = new ArrayList<AbstractOperation>();
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			if(operationConfig.isActive()){
				AbstractOperation operation = operationConfig.createOperation(this, taskManager);
				operations.add(operation);
				propertyContainer.addProperties(
						operationConfig.getId(), 
						operationConfig.getOptionContainer().getProperties()
				);
			}
		}
		
		launchHistory = new LaunchHistory(this, fileManager);
		for(AbstractOperation operation : operations){
			launchHistory.operations.add(new OperationHistory(operation, fileManager));
		}
		
		statusManager.setProgressMax(operations.size());
		aboard = false;
	}
	
	public LaunchConfig getConfig(){ return config; }
	public String getTrigger(){ return trigger; }
	public PropertyContainer getPropertyContainer(){ return propertyContainer; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	
	@Override
	public String getFolder() {
		return 
			fileManager.getBuildFolderPath()+
			File.separator+config.getId();
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup the history-folder
		launchHistory.init();
		
		// setup the logger
		logger.setLogFile(new File(launchHistory.logfile), 0);
		logger.info(Module.COMMON, "Launch ["+config.getName()+"]");
		artifacts.add(new Artifact("Logfile", logger.getLogFile()));
		debugProperties(propertyContainer.getProperties(config.getId()));
		
		// setup launch-folder
		File folder = new File(getFolder());
		if(config.isClean() && folder.isDirectory()){
			logger.log(Module.COMMON, "cleaning: "+folder.getAbsolutePath());
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
				logger.log(Module.COMMON, "Operation: "+operationStatus.toString());	
				if(operationStatus == Status.ERROR){
					if(!operation.getConfig().isCritical()){
						statusManager.setStatus(Status.ERROR);
					}else{
						logger.emph(Module.COMMON, "Critical operation failed");
						statusManager.setStatus(Status.FAILURE);
						aboard = true;
					}
				}else if(operationStatus == Status.FAILURE){
					statusManager.setStatus(Status.FAILURE);
					aboard = true;
				}
			}catch(InterruptedException e){
				logger.emph(Module.COMMON, "Interrupted");
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
				Module.COMMON, 
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
			launchHistory.finish();
			history.add(launchHistory); 
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
		
		if(trigger == AbstractTrigger.USER_TRIGGER){
			try{
				String path = launchHistory.getIndexPath();
				logger.debug(Module.COMMON, "browser: "+path);
				SystemTools.openBrowser(path);
			}catch(IOException e) {
				logger.error(Module.COMMON, e);
			}
		}
		
		logger.info(Module.COMMON, "Launch: "+statusManager.getStatus().toString());
		logger.clearListeners();
	}
	
	private void debugProperties(HashMap<String, String> properties) {
		
		StringBuilder info = new StringBuilder();
		ArrayList<String> keys = PropertyContainer.getKeys(properties);
		for(String key : keys){
			info.append("\t"+key+": "+properties.get(key)+"\n");
		}
		logger.debug(Module.COMMON, "Properties [\n"+info.toString()+"]");
	}
}
