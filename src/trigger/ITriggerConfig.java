package trigger;

import util.AttributeContainer;

/**
 * the configuration of a trigger
 */
public interface ITriggerConfig {

	public String getName();
	public AttributeContainer getContainer();
	public ITrigger createInstance();
}
