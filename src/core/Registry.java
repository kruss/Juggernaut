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

public class Registry {

	private ArrayList<AbstractOperationConfig> operationConfigs;
	private ArrayList<AbstractTriggerConfig> triggerConfigs;
	
	public ArrayList<AbstractOperationConfig> getOperationConfigs(){ return operationConfigs; }	
	public ArrayList<AbstractTriggerConfig> getTriggerConfigs(){ return triggerConfigs; }
	
	public Registry(){
	
		operationConfigs = new ArrayList<AbstractOperationConfig>();
		triggerConfigs = new ArrayList<AbstractTriggerConfig>();
	}
	
	public void init() {
		
		operationConfigs.add(new SampleOperationConfig());
		operationConfigs.add(new CommandOperationConfig());
		operationConfigs.add(new SVNOperationConfig());
		operationConfigs.add(new EclipseOperationConfig());
		
		triggerConfigs.add(new IntervallTriggerConfig());
		triggerConfigs.add(new SVNTriggerConfig());
	}
	
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
				return operationConfig.getClass().newInstance();
			}
		}
		throw new Exception("No class for name: "+name);
	}
	
	public AbstractTriggerConfig createTriggerConfig(String name) throws Exception {

		for(AbstractTriggerConfig triggerConfig : triggerConfigs){
			if(name.equals(triggerConfig.getName())){
				return triggerConfig.getClass().newInstance();
			}
		}
		throw new Exception("No class for name: "+name);
	}
}
