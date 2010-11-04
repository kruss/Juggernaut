package model;

/**
 * the configuration of an operation
 */
public interface IOperationConfig {

	public AttributeContainer getContainer();
	public IOperation createInstance();
	public Class<?> getType();
}
