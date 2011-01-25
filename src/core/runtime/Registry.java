package core.runtime;

import java.util.ArrayList;

import core.ISystemComponent;
import core.launch.operation.AbstractOperationConfig;
import core.launch.operation.CommandOperationConfig;
import core.launch.operation.EclipseOperationConfig;
import core.launch.operation.SVNOperationConfig;
import core.launch.operation.SampleOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.launch.trigger.IntervallTriggerConfig;
import core.launch.trigger.SVNTriggerConfig;
import core.launch.trigger.TimedTriggerConfig;
import core.runtime.logger.Logger;

/** runtime object factory */
public class Registry implements ISystemComponent {

	private TaskManager taskManager;
	private Logger logger;
	
	private ArrayList<Class<?>> operationConfigs;
	private ArrayList<Class<?>> triggerConfigs;
	
	public Registry(
			TaskManager taskManager, 
			Logger logger)
	{
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
		triggerConfigs.add(TimedTriggerConfig.class);
	}
	
	@Override
	public void shutdown() throws Exception {}
	
	public ArrayList<String> getOperationNames() throws Exception {
		
		ArrayList<String> names = new ArrayList<String>();
		for(Class<?> clazz : operationConfigs){
			names.add(getOperationName(clazz));
		}
		return names;
	}
	
	private String getOperationName(Class<?> clazz) throws Exception {
		return ((AbstractOperationConfig) clazz.newInstance()).getName();
	}

	public ArrayList<String> getTriggerNames() throws Exception {
		
		ArrayList<String> names = new ArrayList<String>();
		for(Class<?> clazz : triggerConfigs){
			names.add(getTriggerName(clazz));
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
				instance.initInstance(taskManager, logger);
				return instance;
			}
		}
		return null;
	}
	
	public AbstractTriggerConfig createTriggerConfig(String name) throws Exception {

		for(Class<?> clazz : triggerConfigs){
			if(name.equals(getTriggerName(clazz))){
				AbstractTriggerConfig instance = (AbstractTriggerConfig) clazz.newInstance();
				instance.initInstance(taskManager, logger);
				return instance;
			}
		}
		return null;
	}
}
