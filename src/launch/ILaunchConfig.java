package launch;

import java.util.ArrayList;

import operation.IOperationConfig;
import trigger.ITriggerConfig;
import util.AttributeContainer;


/**
 * the configuration of a launch
 */
public interface ILaunchConfig {

	public AttributeContainer getContainer();

	public ArrayList<IOperationConfig> getOperations();
	public ArrayList<ITriggerConfig> getTriggers();

	public boolean isDirty();
	public void setDirty(boolean dirty);
	
	public ILaunch createInstance();
}
