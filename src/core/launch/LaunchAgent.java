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
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.logger.Logger.Mode;
import core.runtime.smtp.ISmtpClient;
import util.FileTools;
import util.SystemTools;


public class LaunchAgent extends LifecycleObject {

	private enum PROPERTY { NAME, FOLDER, START }
	
	private History history;
	private FileManager fileManager;
	private LaunchConfig launchConfig;
	private String trigger;
	private PropertyContainer propertyContainer;
	private ArrayList<AbstractOperation> operations;
	private NotificationManager notificationManager;
	private LaunchHistory launchHistory;
	private Logger logger;
	private boolean aboard;
	
	public LaunchHistory getHistory(){ return launchHistory; }
	
	public LaunchAgent(
			Configuration configuration, 
			Cache cache,
			History history, 
			FileManager fileManager, 
			TaskManager taskManager, 
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			LaunchConfig launchConfig,
			String trigger)
	{
		super("Launch::"+launchConfig.getName()+"::"+launchConfig.getId(), taskManager);
		
		this.fileManager = fileManager;
		this.history = history;
		this.launchConfig = launchConfig.clone();
		this.trigger = trigger;
		logger = new Logger(Mode.FILE);
		logger.setConfig(configuration.getLogConfig());
		
		propertyContainer = new PropertyContainer();
		
		operations = new ArrayList<AbstractOperation>();
		for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
			if(operationConfig.isActive()){
				AbstractOperation operation = operationConfig.createOperation(configuration, cache, taskManager, this);
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
	}
	
	public LaunchConfig getConfig(){ return launchConfig; }
	public String getTrigger(){ return trigger; }
	public PropertyContainer getPropertyContainer(){ return propertyContainer; }
	public ArrayList<AbstractOperation> getOperations(){ return operations; }
	
	@Override
	public String getId() {
		return launchConfig.getId();
	}
	@Override
	public String getFolder() {
		return fileManager.getLaunchFolderPath(launchConfig.getId());
	}
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// setup the history-folder
		launchHistory.init();
		
		// setup the logger
		logger.setLogFile(new File(launchHistory.logfile), 0);
		logger.info(Module.COMMON, "Launch ["+launchConfig.getName()+"]");
		artifacts.add(new Artifact("Logfile", logger.getLogFile()));
		
		// setup launch-folder
		File folder = new File(getFolder());
		logger.log(Module.COMMON, "Folder: "+folder.getAbsolutePath());
		if(launchConfig.isClean() && folder.isDirectory()){
			try{
				FileTools.deleteFolder(folder.getAbsolutePath());
			}catch(Exception e){
				if(fileManager.hasUnlocker()){
					logger.log(Module.COMMON, "Unable to delete ("+e.getMessage()+") => Retry with unlocker");
					fileManager.deleteWithUnlocker(folder, logger);
				}else{
					throw e;
				}
			}
		}
		if(!folder.isDirectory()){
			FileTools.createFolder(folder.getAbsolutePath());
		}
		
		// set properties
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.NAME.toString(), launchConfig.getName())
		);
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.FOLDER.toString(), getFolder())
		);
		propertyContainer.setProperty(
				new Property(launchConfig.getId(), PROPERTY.START.toString(), ""+statusManager.getStart().getTime())
		);
		
		// debug options
		logger.debug(Module.COMMON, "Settings:\n"+launchConfig.getOptionContainer().toString());
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
				operation.getStatusManager().setStatus(Status.CANCEL);
				operation.asyncStop(1000);
			}finally{
				statusManager.addProgress(1);
			}
		}
	}

	private Status executeOperation(AbstractOperation operation)throws Exception {
		
		logger.info(
				Module.COMMON, 
				operation.getIndex()+"/"+launchConfig.getOperationConfigs().size()+
				" Operation ["+operation.getConfig().getName()+"]"
		);
		if(!aboard){
			operation.syncRun(0, 0);
		}else{
			operation.getStatusManager().setStatus(Status.CANCEL);
		}
		return operation.getStatusManager().getStatus();
	}

	@Override
	protected void finish() {

		logger.info(Module.COMMON, "Output");
		
		// perform notification
		try{ 
			notificationManager.performNotification();
		}catch(Exception e){
			logger.error(Module.SMTP, e);
		}
		
		// add to history
		try{ 
			launchHistory.finish(); 
			history.add(launchHistory);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
		
		// open browser
		if(trigger == AbstractTrigger.USER_TRIGGER){
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
	
	public ArrayList<Error> getErrors(){
		
		ArrayList<Error> errors = new ArrayList<Error>();
		for(AbstractOperation operation : operations){
				errors.addAll(operation.getErrors());
		}
		return errors;
	}
}
