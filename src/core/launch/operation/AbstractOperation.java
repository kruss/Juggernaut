package core.launch.operation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import core.launch.LaunchAgent;
import core.launch.LifecycleObject;
import core.launch.data.Artifact;
import core.launch.data.Error;
import core.launch.data.StatusManager.Status;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

import util.FileTools;



public abstract class AbstractOperation extends LifecycleObject {

	protected Configuration configuration;
	protected Cache cache;
	protected LaunchAgent parent;
	protected TaskManager taskManager;
	protected Logger logger;
	protected AbstractOperationConfig config;
	protected ArrayList<Error> errors;
	
	public LaunchAgent getParent(){ return parent; }
	public AbstractOperationConfig getConfig(){ return config; }

	public AbstractOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent, 
			AbstractOperationConfig config)
	{
		super("Opperation("+config.getId()+")", taskManager);
		
		this.configuration = configuration;
		this.cache = cache;
		this.parent = parent;
		this.taskManager = taskManager;
		logger = parent.getLogger();
		this.config = config.clone();
		errors = new ArrayList<Error>();
	}
	
	public ArrayList<Error> getErrors(){ return errors; }
	public void addError(String message){
		errors.add(new Error(this, message));
		statusManager.setStatus(Status.ERROR);
	}
	
	/** returns the 1-based index of this operation within the launch */
	public int getIndex() {

		int index = 1;
		for(AbstractOperationConfig config : parent.getConfig().getOperationConfigs()){
			if(config.getId().equals(this.config.getId())){
				break;
			}
			index++;
		}
		return index;
	}

	/** provides the runtime description */
	public abstract String getDescription();
	
	/** provides name +index within launch as identifier */
	public String getIdentifier() {
		return config.getName()+"["+getIndex()+"]";
	}
	
	public void setParent(LaunchAgent parent){ this.parent = parent; }
	
	@Override
	public String getFolder() {
		return parent.getFolder();
	}
	
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		// expand properties
		config.getOptionContainer().expand(parent.getPropertyContainer());
		// debug options
		logger.debug(Module.COMMON, "Settings:\n"+config.getOptionContainer().toString());		
	}
	
	@Override
	protected void finish() {
		
		Status status = statusManager.getStatus();
		if(status != Status.SUCCEED && status != Status.CANCEL){
			if(errors.size() == 0){
				addError("Operation did not succeed");
			}
		}
	}
	
	/** copy a relative output-folder to history */
	public void collectOuttput(String outputFolder) {
		
		File source = new File(getFolder()+File.separator+outputFolder);
		File destination = new File(historyFolder+File.separator+outputFolder);
		if(source.isDirectory() && destination.mkdirs()){
			logger.debug(Module.COMMAND, "Collecting output: "+source.getAbsolutePath());
			try{
				FileTools.copyFolder(source.getAbsolutePath(), destination.getAbsolutePath());
				artifacts.add(new Artifact("Output ["+outputFolder+"]", destination));
			}catch(IOException e){
				logger.error(Module.COMMAND, e);
			}
		}
	}
}
