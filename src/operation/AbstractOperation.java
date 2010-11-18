package operation;

import java.io.File;

import util.Logger;

import launch.LaunchAction;
import lifecycle.AbstractLifecycleObject;

public abstract class AbstractOperation extends AbstractLifecycleObject {

	protected LaunchAction parent;
	protected AbstractOperationConfig config;
	protected Logger logger;
	
	public AbstractOperation(AbstractOperationConfig config){
		
		this.config = config.clone();
	}
	
	public void setParent(LaunchAction parent){ this.parent = parent; }

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
	protected void finish() throws Exception {
		// TODO Auto-generated method stub
	}
}
