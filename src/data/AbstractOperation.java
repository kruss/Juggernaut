package data;

import java.io.File;
import java.io.IOException;

import core.Cache;
import core.Configuration;
import core.TaskManager;

import util.FileTools;

import launch.LifecycleObject;
import launch.LaunchAgent;
import launch.StatusManager;

import logger.Logger;
import logger.ILogConfig.Module;

public abstract class AbstractOperation extends LifecycleObject {

	protected Configuration configuration;
	protected Cache cache;
	protected LaunchAgent parent;
	protected TaskManager taskManager;
	protected Logger logger;
	protected AbstractOperationConfig config;
	
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
		
		parent.getPropertyContainer().addProperties(
				config.getId(), config.getOptionContainer().getProperties()
		);
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

	public String getDescription() {
		return "Index: "+getIndex();
	}
	
	public void setParent(LaunchAgent parent){ this.parent = parent; }
	
	@Override
	public String getFolder() {
		return parent.getFolder();
	}
	
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {}
	
	@Override
	protected void finish() {
		
		if(StatusManager.isError(statusManager.getStatus())){
			statusManager.addError(config.getId(), "Operation did not succeed");
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
