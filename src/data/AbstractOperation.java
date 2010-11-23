package data;

import util.Logger;

import lifecycle.AbstractLifecycleObject;
import lifecycle.LaunchAgent;

public abstract class AbstractOperation extends AbstractLifecycleObject {

	protected LaunchAgent parent;
	protected AbstractOperationConfig config;
	protected Logger logger;
	
	public AbstractOperationConfig getConfig(){ return config; }

	public AbstractOperation(AbstractOperationConfig config){
		
		this.config = config.clone();
		setName(parent.getName()+"::"+config.getName());
	}
	
	public void setParent(LaunchAgent parent){ this.parent = parent; }
	
	@Override
	public String getOutputFolder() {
		return parent.getOutputFolder();
	}
	
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		logger = parent.getLogger();
	}
	
	@Override
	protected void finish() {
		// TODO Auto-generated method stub
	}
}
