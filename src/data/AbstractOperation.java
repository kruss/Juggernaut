package data;

import launch.LifecycleObject;
import launch.LaunchAgent;
import logger.Logger;

public abstract class AbstractOperation extends LifecycleObject {

	protected transient LaunchAgent parent;
	protected transient Logger logger;
	protected AbstractOperationConfig config;
	
	public LaunchAgent getParent(){ return parent; }
	public AbstractOperationConfig getConfig(){ return config; }

	public AbstractOperation(LaunchAgent parent, AbstractOperationConfig config){
		
		super("Opperation("+config.getId()+")");
		this.parent = parent;
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
	protected void finish() {}
}
