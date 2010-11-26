package lifecycle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import core.Application;
import data.AbstractOperation;
import data.AbstractOperationConfig;
import data.Artifact;
import data.HistoryEntry;
import data.LaunchConfig;
import data.Artifact.Attachment;
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
	private Logger logger;
	
	public LaunchAgent(LaunchConfig config, TriggerStatus trigger){

		this.application = Application.getInstance();
		
		this.config = config.clone();
		this.triggerStatus = trigger;
		
		setName("Launch("+config.getName()+")");
		logger = new Logger(Mode.FILE_ONLY);
		logger.setLogiFile(new File(getLogfile()));
		
		propertyManager = new PropertyManager();
		propertyManager.addProperty(config.getId(), "Name", config.getName());
		propertyManager.addProperty(config.getId(), "Folder", getFolder());
		propertyManager.addProperty(config.getId(), "Trigger", trigger.message);
		propertyManager.addProperty(config.getId(), "Clean", ""+config.isClean());
		propertyManager.addProperty(config.getId(), "Timeout", StringTools.millis2min(config.getTimeout())+" min");
		
		statusManager.setProgressMax(config.getOperationConfigs().size());
		operations = new ArrayList<AbstractOperation>();
	}
	
	public LaunchConfig getConfig(){ return config; }
	public TriggerStatus getTriggerStatus(){ return triggerStatus; }
	public PropertyManager getPropertyManager(){ return propertyManager; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	
	@Override
	public String getFolder() {
		return Application.getInstance().getBuildFolder()+File.separator+config.getId();
	}
	
	public String getLogfile() {
		return Application.getInstance().getBuildFolder()+File.separator+config.getId()+".log";
	}

	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup launch-folder
		File folder = new File(getFolder());
		if(config.isClean() && folder.isDirectory()){
			FileTools.deleteFolder(folder.getAbsolutePath());
		}
		if(!folder.isDirectory()){
			FileTools.createFolder(folder.getAbsolutePath());
		}
		
		// create operations
		for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
			AbstractOperation operation = operationConfig.createOperation(this);
			operations.add(operation);
			propertyManager.addProperties(
					operationConfig.getId(), 
					operationConfig.getOptionContainer().getProperties()
			);
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

		// process status
		propertyManager.addProperties(
				config.getId(), 
				statusManager.getProperties()
		);
		logger.info("Status: "+statusManager.getStatus().toString());
		
		
		// perform notification
		// TODO

		// create artifacts
		Artifact logfileArtifact = new Artifact(
				Artifact.Type.GENERATED.toString(), "Logifile"
		);
		Attachment logfileAttachment = logfileArtifact.new Attachment(
				Artifact.Name.LOGFILE.toString(), logger.getLogfile().getAbsolutePath()
		);
		logfileArtifact.attachments.add(logfileAttachment);
		artifacts.add(logfileArtifact);
		
		// perform output
		HistoryEntry entry = new HistoryEntry(this);
		try{ 
			application.getHistory().addEntry(entry); 
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
