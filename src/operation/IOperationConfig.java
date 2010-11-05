package operation;

import util.OptionContainer;

/**
 * the configuration of an operation
 */
public interface IOperationConfig {

	public String getName();
	public OptionContainer getOptionContainer();
	public IOperation createOperation();
	public String toString();
}
