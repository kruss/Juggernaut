package trigger;

import util.Attribute;
import util.Attribute.Type;

public class IntervallTriggerConfig extends AbstractTriggerConfig {

	public enum ATTRIBUTES {
		INTERVALL
	}
	
	public IntervallTriggerConfig(){
		
		container.getAttributes().add(new Attribute(
				ATTRIBUTES.INTERVALL.toString(), "Intervall in minutes (0 = continous)", 
				Type.INTEGER, 0
		));
	}
	
	@Override
	public ITrigger createInstance() {
		// TODO
		// return (new TriggerInitializer(this)).createInstance();
		return null;
	}
}
