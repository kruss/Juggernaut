package core.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import core.launch.data.Artifact;
import core.launch.data.Error;
import core.launch.data.StatusManager.Status;
import core.launch.data.property.Property;
import core.launch.data.property.PropertyContainer;
import core.launch.history.LaunchHistory;
import core.launch.history.OperationHistory;
import core.launch.notification.NotificationManager;
import core.launch.operation.AbstractOperation;
import core.launch.operation.AbstractOperationConfig;
import core.launch.operation.IRepositoryOperation;
import core.launch.trigger.AbstractTrigger;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.FileManager;
import core.runtime.TaskManager;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ErrorManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.logger.Logger.Mode;
import core.runtime.smtp.ISmtpClient;
import util.DateTools;
import util.FileTools;
import util.SystemTools;


public class LaunchAgent extends LifecycleObject {

	public enum PROPERTY { NAME, FOLDER, START }
	
	public enum LaunchMode { AUTOMATED, USER, SEARCH }
	
	private Logger logger;
	private History history;
	private FileManager fileManager;
	private LaunchConfig launchConfig;
	private AbstractTrigger trigger;
	private PropertyContainer propertyContainer;
	private ArrayList<AbstractOperation> operations;
	private NotificationManager notificationManager;
	private LaunchHistory launchHistory;
	
	private boolean aboard;
	private LaunchMode mode;
	
	public LaunchHistory getHistory(){ return launchHistory; }
	
	public LaunchAgent(
			ErrorManager errorManager,
			Configuration configuration, 
			Cache cache,
			History history, 
			FileManager fileManager, 
			TaskManager taskManager, 
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			LaunchConfig launchConfig,
			AbstractTrigger trigger)
	{
		super("Launch::"+launchConfig.getName()+"::"+launchConfig.getId(), taskManager);

		logger = new Logger(errorManager, Mode.FILE);
		logger.setConfig(configuration.getLogConfig());
		
		this.history = history;
		this.fileManager = fileManager;
		this.launchConfig = launchConfig.clone();
		this.trigger = trigger;
		
		propertyContainer = new PropertyContainer();
		propertyContainer.setProperties(configuration.getSystemProperties());
		
		operations = new ArrayList<AbstractOperation>();
		for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
			if(operationConfig.isActive()){
				AbstractOperation operation = operationConfig.createOperation(cache, taskManager, this);
				operations.add(operation);
			}
		}
		
		notificationManager = new NotificationManager(history, cache, smtpClient, httpServer, this);
		
		launchHistory = new LaunchHistory(this, fileManager);
		for(AbstractOperation operation : operations){
			launchHistory.operations.add(new OperationHistory(operation, fileManager));
		}
		
		statusManager.setProgressMax(operations.size());
		aboard = false;
		mode = LaunchMode.AUTOMATED;
	}
	
	public LaunchConfig getConfig(){ return launchConfig; }
	public AbstractTrigger getTrigger(){ return trigger; }
	public PropertyContainer getPropertyContainer(){ return propertyContainer; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	public void setMode(LaunchMode mode){ this.mode = mode; }
	public LaunchMode getMode(){ return mode; }
	
	@Override
	public String getId() {
		return launchConfig.getId();
	}
	@Override
	public String getIdentifier() {
		return launchConfig.getName();
	}
	@Override
	public String getFolder() {
		return fileManager.getLaunchFolderPath(launchConfig.getId());
	}
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup history-folder
		launchHistory.init();
		
		// setup logger
		logger.setLogFile(new File(launchHistory.logfile), 0);
		logger.info(Module.COMMON, "Launch ["+launchConfig.getName()+"]");
		artifacts.add(new Artifact("Logfile", logger.getLogFile()));
		
		// setup launch-folder
		File folder = new File(getFolder());
		if(launchConfig.isClean() && folder.isDirectory()){
			try{
				logger.log(Module.COMMON, "delete: "+folder.getAbsolutePath());
				FileTools.deleteFolder(folder.getAbsolutePath());
			}catch(Exception e){
				if(fileManager.hasUnlocker()){
					logger.error(Module.COMMON, e.getMessage());
					fileManager.deleteWithUnlocker(folder, logger);
				}else{
					throw e;
				}
			}
		}
		if(!folder.isDirectory()){
			logger.log(Module.COMMON, "create: "+folder.getAbsolutePath());
			FileTools.createFolder(folder.getAbsolutePath());
		}
		
		// apply system-properties
		launchConfig.getOptionContainer().expand(propertyContainer);
		// set launch-properties
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.NAME.toString(), launchConfig.getName())
		);
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.FOLDER.toString(), getFolder())
		);
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.START.toString(), DateTools.getTextDate(statusManager.getStart()))
		);
		
		// debug configuration
		logger.debug(Module.COMMON, "Configuration:\n"+launchConfig.getOptionContainer().toString());
		artifacts.add(new Artifact("Configuration", new ConfigPage(launchConfig.getName(), launchConfig.getOptionContainer()).getHtml(), "htm"));
	}
	
	@Override
	protected void execute() throws Exception {
		
		for(AbstractOperation operation : operations){
			try{
				Status status = executeOperation(operation);
				String identifier = operation.getIdentifier();
				logger.log(Module.COMMON, "Status: "+status.toString());	
				if(status == Status.ERROR){
					if(!operation.getConfig().isCritical()){
						statusManager.addError(this, null, identifier+" with status "+status.toString());
						statusManager.setStatus(Status.ERROR);
					}else{
						statusManager.addError(this, null, identifier+" with CRITICAL status "+status.toString());
						statusManager.setStatus(Status.FAILURE);
						aboard = true;
					}
				}else if(status == Status.FAILURE){
					statusManager.addError(this, null, identifier+" with status "+status.toString());
					statusManager.setStatus(Status.FAILURE);
					aboard = true;
				}
			}catch(InterruptedException e){
				logger.emph(Module.COMMON, "Interrupted <"+getThreadId()+">");
				statusManager.setStatus(Status.CANCEL);
				aboard = true;
				operation.getStatusManager().setStatus(Status.CANCEL);
				operation.asyncStop(1000);
			}finally{
				statusManager.addProgress(1);
			}
		}
	}

	private Status executeOperation(AbstractOperation operation)throws Exception {
		
		logger.info(Module.COMMON, 
				operation.getIndex()+"/"+launchConfig.getOperationConfigs().size()+
				" Operation ["+operation.getConfig().getUIName()+"]" +
				(operation.getConfig().isCritical() ? " - CRITICAL" : "")
		);
		if(!aboard){
			operation.syncRun(0, timeout);
		}else{
			operation.getStatusManager().setStatus(Status.CANCEL);
		}
		return operation.getStatusManager().getStatus();
	}

	@Override
	protected void finish() {

		logger.info(Module.COMMON, "Finish");
		
		// check final status
		Status status = statusManager.getStatus();
		if(status != Status.SUCCEED && status != Status.CANCEL){
			if(statusManager.getErrors().size() == 0){
				statusManager.addError(this, null, "Launch did not succeed");
			}
		}
		
		// perform notification
		try{ 
			notificationManager.performNotification();
		}catch(Exception e){
			logger.error(Module.SMTP, e);
		}
		
		// add to history
		try{ 
			logger.log(Module.COMMON, "output: "+launchHistory.folder);
			launchHistory.finish(); 
			history.add(launchHistory);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
		
		// open browser
		if(mode == LaunchMode.USER){
			try{
				String path = launchHistory.getIndexPath();
				logger.debug(Module.COMMON, "browser: "+path);
				SystemTools.openBrowser(path);
			}catch(IOException e) {
				logger.error(Module.COMMON, e);
			}
		}
		
		// close logging
		logger.info(Module.COMMON, statusManager.getStatus().toString());
		logger.clearListeners();
	}
	
	public ArrayList<IRepositoryOperation> getRepositoryOperations(){
		
		ArrayList<IRepositoryOperation> list = new ArrayList<IRepositoryOperation>();
		for(AbstractOperation operation : operations){
			if(operation instanceof IRepositoryOperation){
				list.add((IRepositoryOperation) operation);
			}	
		}
		return list;
	}
	
	public long getLaunchStatusHash() {
		
		long hash = statusManager.getHash();
		for(AbstractOperation operation : operations){
			hash += operation.getStatusManager().getHash();
		}
		return hash;
	}
	
	public long getOperationErrorHash() {
		
		long hash = 0;
		for(Error error : getOperationErrors()){
			hash += error.getHash();
		}
		return hash;
	}
	
	public ArrayList<Error> getOperationErrors(){
		
		ArrayList<Error> errors = new ArrayList<Error>();
		for(AbstractOperation operation : operations){
				errors.addAll(operation.getStatusManager().getErrors());
		}
		return errors;
	}
}
