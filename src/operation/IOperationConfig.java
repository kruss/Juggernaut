package operation;

import util.AttributeContainer;

/**
 * the configuration of an operation
 */
public interface IOperationConfig {

	public String getName();
	public AttributeContainer getContainer();
	public IOperation createInstance();
}
