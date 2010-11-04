package model;

/**
 * the configuration of a trigger
 */
public interface ITriggerConfig {

	public AttributeContainer getContainer();
	public ITrigger createInstance();
	public Class<?> getType();
}
