package trigger;

import java.util.Date;

import util.StringTools;

import data.AbstractTrigger;
import data.AbstractTriggerConfig;

public class UserTrigger extends AbstractTrigger {

	public UserTrigger() {
		
		super(new AbstractTriggerConfig(){
			@Override
			public String getName() {
				return "User";
			}
			@Override
			public String getDescription() {
				return "A trigger executed by the user";
			}
			@Override
			public boolean isValid() {
				return true;
			}
			@Override
			public AbstractTrigger createTrigger() {
				return null;
			}
		});
		
		triggerReason = "Triggered at "+StringTools.getTextDate(new Date());
	}

	@Override
	public void init() {}

	@Override
	public boolean isTriggered() {
		return true;
	}
}
