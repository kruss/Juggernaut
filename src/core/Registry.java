package core;

import java.util.ArrayList;

import operation.CommandOperationConfig;
import operation.EclipseOperationConfig;
import operation.SVNOperationConfig;
import operation.SampleOperationConfig;
import trigger.IntervallTriggerConfig;
import trigger.SVNTriggerConfig;

import data.AbstractOperationConfig;
import data.AbstractTriggerConfig;

public class Registry implements ISystemComponent {

	private Configuration configuration;
	private Cache cache;
	
	private ArrayList<AbstractOperationConfig> operationConfigs;
	private ArrayList<AbstractTriggerConfig> triggerConfigs;
	
	public ArrayList<AbstractOperationConfig> getOperationConfigs(){ return operationConfigs; }	
	public ArrayList<AbstractTriggerConfig> getTriggerConfigs(){ return triggerConfigs; }
	
	public Registry(Configuration configuration, Cache cache){
	
		this.configuration = configuration;
		this.cache = cache;
		
		operationConfigs = new ArrayList<AbstractOperationConfig>();
		triggerConfigs = new ArrayList<AbstractTriggerConfig>();
	}
	
	@Override
	public void init() throws Exception {
		
		operationConfigs.clear();
		operationConfigs.add(new SampleOperationConfig());
		operationConfigs.add(new CommandOperationConfig());
		operationConfigs.add(new SVNOperationConfig());
		operationConfigs.add(new EclipseOperationConfig());
		
		triggerConfigs.clear();
		triggerConfigs.add(new IntervallTriggerConfig());
		triggerConfigs.add(new SVNTriggerConfig());
	}
	
	@Override
	public void shutdown() throws Exception {}
	
	public ArrayList<String> getOperationNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(AbstractOperationConfig operationConfig : operationConfigs){
			names.add(operationConfig.getName());
		}
		return names;
	}
	
	public ArrayList<String> getTriggerNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(AbstractTriggerConfig triggerConfig : triggerConfigs){
			names.add(triggerConfig.getName());
		}
		return names;
	}

	public AbstractOperationConfig createOperationConfig(String name) throws Exception {

		for(AbstractOperationConfig operationConfig : operationConfigs){
			if(name.equals(operationConfig.getName())){
				AbstractOperationConfig instance = operationConfig.getClass().newInstance();
				instance.init(configuration, cache);
				return instance;
			}
		}
		throw new Exception("No class for name: "+name);
	}
	
	public AbstractTriggerConfig createTriggerConfig(String name) throws Exception {

		for(AbstractTriggerConfig triggerConfig : triggerConfigs){
			if(name.equals(triggerConfig.getName())){
				AbstractTriggerConfig instance = triggerConfig.getClass().newInstance();
				instance.init(configuration, cache);
				return instance;
			}
		}
		throw new Exception("No class for name: "+name);
	}
}
