package operation;

import java.util.ArrayList;

public class OperationRegistry {

	private ArrayList<AbstractOperationConfig> configs;
	
	public OperationRegistry(){
	
		configs = new ArrayList<AbstractOperationConfig>();
	}
	
	public ArrayList<AbstractOperationConfig> getOperationConfigs() {
		return configs;
	}
}
