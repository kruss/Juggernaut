package trigger;

import util.Option;
import util.Option.Type;

public class IntervallTriggerConfig extends AbstractTriggerConfig {

	public enum OPTIONS {
		INTERVALL
	}
	
	public IntervallTriggerConfig(){
		
		container.getOptions().add(new Option(
				OPTIONS.INTERVALL.toString(), "Intervall in minutes (0 = continous)", 
				Type.INTEGER, 0
		));
	}
	
	@Override
	public ITrigger createTrigger() {
		// TODO
		// return (new TriggerInitializer(this)).createInstance();
		return null;
	}
}
