package trigger;

import util.Option;
import util.Option.Type;

public class IntervallTriggerConfig extends AbstractTriggerConfig {

	public static final String TRIGGER_NAME = "Intervall";
	
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
	public String getName(){ return TRIGGER_NAME; }
	
	@Override
	public AbstractTrigger createTrigger() {
		// TODO
		// return (new TriggerInitializer(this)).createInstance();
		return null;
	}
}
