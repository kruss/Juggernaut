package data;

import java.io.File;


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
	}
	
	public void setParent(LaunchAgent parent){ this.parent = parent; }
	
	@Override
	public String getOutputFolder() {
		return parent.getOutputFolder()+File.separator+config.getId();
	}
	
	@Override
	public Logger getLogger() { return logger; }
	
	@Override
	protected void init() throws Exception {
		
		File folder = new File(getOutputFolder());
		if(!folder.isDirectory()){
			folder.mkdirs();
		}
		logger = parent.getLogger();
	}
	
	@Override
	protected void finish() {
		// TODO Auto-generated method stub
	}
}
