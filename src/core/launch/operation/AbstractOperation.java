package core.launch.operation;

import java.io.File;

import core.Feedback;
import core.Result;
import core.launch.LaunchAgent;
import core.launch.LifecycleObject;
import core.launch.data.Artifact;
import core.launch.data.ResultManager;
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
	protected ResultManager resultManager;
	
	public LaunchAgent getParent(){ return parent; }
	public AbstractOperationConfig getConfig(){ return config; }	
	public ResultManager getResultManager(){ return resultManager; }

	public AbstractOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent, 
			AbstractOperationConfig config)
	{
		super("Opperation::"+config.getUIName()+"::"+config.getId(), taskManager);
		
		this.configuration = configuration;
		this.cache = cache;
		this.parent = parent;
		this.taskManager = taskManager;
		logger = parent.getLogger();
		this.config = config.clone();
		resultManager = new ResultManager(statusManager);
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
	public abstract String getRuntimeDescription();
	
	/** provides name+index as identifier */
	public String getIdentifier() {
		return config.getUIName()+"["+getIndex()+"]";
	}
	
	@Override
	public String getId() {
		return config.getId();
	}
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
		
		// debug configuration
		logger.debug(Module.COMMON, "Configuration:\n"+config.getOptionContainer().toString());		
		artifacts.add(new Artifact("Configuration", new ConfigPage(config.getUIName(), config.getOptionContainer()).getHtml(), "htm"));
	}
	
	@Override
	protected void finish() {
		
		// check final status
		Status status = statusManager.getStatus();
		if(status != Status.SUCCEED && status != Status.CANCEL){
			if(statusManager.getErrors().size() == 0){
				statusManager.addError(this, "Operation did not succeed");
			}
		}
	}
	
	/** copy a relative output-folder to history */
	public void handleOutput(String outputFolder) {
		
		File source = new File(getFolder()+File.separator+outputFolder);
		if(source.isDirectory()){
			// get results
			File file = new File(source+File.separator+Feedback.OUTPUT_FILE);
			if(file.isFile()){
				logger.debug(Module.COMMAND, "Collecting Feedback: "+file.getAbsolutePath());
				try{
					Feedback feedback = new Feedback();
					feedback.deserialize(file.getAbsolutePath());
					for(Result result : feedback.results){
						resultManager.addResult(result);
					}
				}catch(Exception e){
					logger.error(Module.COMMAND, e);
				}
			}
			// copy output
			File destination = new File(historyFolder+File.separator+outputFolder);
			if(destination.mkdirs()){
				logger.debug(Module.COMMAND, "Collecting Output: "+source.getAbsolutePath());
				try{
					FileTools.copyFolder(source.getAbsolutePath(), destination.getAbsolutePath());
					artifacts.add(new Artifact("Output ["+outputFolder+"]", destination));
				}catch(Exception e){
					logger.error(Module.COMMAND, e);
				}
			}
		}
	}
}
