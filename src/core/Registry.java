package core;

import java.util.ArrayList;

import logger.Logger;
import logger.Logger.Module;

import operation.CommandOperationConfig;
import operation.EclipseOperationConfig;
import operation.SVNOperationConfig;
import operation.SampleOperationConfig;
import trigger.IntervallTriggerConfig;
import trigger.SVNTriggerConfig;

import data.AbstractOperationConfig;
import data.AbstractTriggerConfig;

/** runtime object factory */
public class Registry implements ISystemComponent {

	private Configuration configuration;
	private Cache cache;
	private TaskManager taskManager;
	private Logger logger;
	
	private ArrayList<Class<?>> operationConfigs;
	private ArrayList<Class<?>> triggerConfigs;
	
	public Registry(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		this.configuration = configuration;
		this.cache = cache;
		this.taskManager = taskManager;
		this.logger = logger;
		
		operationConfigs = new ArrayList<Class<?>>();
		triggerConfigs = new ArrayList<Class<?>>();
	}
	
	@Override
	public void init() throws Exception {
		
		operationConfigs.clear();
		operationConfigs.add(SampleOperationConfig.class);
		operationConfigs.add(CommandOperationConfig.class);
		operationConfigs.add(SVNOperationConfig.class);
		operationConfigs.add(EclipseOperationConfig.class);
		
		triggerConfigs.clear();
		triggerConfigs.add(IntervallTriggerConfig.class);
		triggerConfigs.add(SVNTriggerConfig.class);
	}
	
	@Override
	public void shutdown() throws Exception {}
	
	public ArrayList<String> getOperationNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(Class<?> clazz : operationConfigs){
			try{
				names.add(getOperationName(clazz));
			}catch(Exception e) {
				logger.error(Module.COMMON, e);
			}
		}
		return names;
	}
	
	private String getOperationName(Class<?> clazz) throws Exception {
		return ((AbstractOperationConfig) clazz.newInstance()).getName();
	}

	public ArrayList<String> getTriggerNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(Class<?> clazz : triggerConfigs){
			try{
				names.add(getTriggerName(clazz));
			}catch(Exception e) {
				logger.error(Module.COMMON, e);
			}
		}
		return names;
	}
	
	private String getTriggerName(Class<?> clazz) throws Exception {
		return ((AbstractTriggerConfig) clazz.newInstance()).getName();
	}

	public AbstractOperationConfig createOperationConfig(String name) throws Exception {

		for(Class<?> clazz : operationConfigs){
			if(name.equals(getOperationName(clazz))){
				AbstractOperationConfig instance = (AbstractOperationConfig) clazz.newInstance();
				instance.initInstance(configuration, cache, taskManager, logger);
				return instance;
			}
		}
		throw new Exception("No class for name: "+name);
	}
	
	public AbstractTriggerConfig createTriggerConfig(String name) throws Exception {

		for(Class<?> clazz : triggerConfigs){
			if(name.equals(getTriggerName(clazz))){
				AbstractTriggerConfig instance = (AbstractTriggerConfig) clazz.newInstance();
				instance.initInstance(configuration, cache, taskManager, logger);
				return instance;
			}
		}
		throw new Exception("No class for name: "+name);
	}
}
