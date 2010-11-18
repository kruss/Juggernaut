package operation;

public class SampleOperation extends AbstractOperation {

	public SampleOperation(SampleOperationConfig config) {
		super(config);
	}

	@Override
	protected void execute() throws Exception {
		// TODO Auto-generated method stub
		logger.log("Yeah...");
	}
}
