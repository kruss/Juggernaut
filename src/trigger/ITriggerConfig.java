package trigger;

import util.OptionContainer;

/**
 * the configuration of a trigger
 */
public interface ITriggerConfig {

	public String getName();
	public OptionContainer getOptionContainer();
	public ITrigger createTrigger();
	public String toString();
}
