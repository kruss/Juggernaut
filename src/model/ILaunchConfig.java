package model;

import java.util.ArrayList;

/**
 * the configuration of a launch
 */
public interface ILaunchConfig {

	public AttributeContainer getContainer();

	public ArrayList<IOperationConfig> getOperations();
	public ArrayList<ITriggerConfig> getTriggers();

	public ILaunch createInstance();
}
