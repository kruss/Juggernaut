package core.launch.trigger;

import core.launch.LaunchAgent;
import core.launch.LaunchAgent.LaunchMode;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.logger.Logger;

public class UserTrigger extends AbstractTrigger {
	
	public UserTrigger(Configuration configuration, Cache cache, Logger logger, UserTriggerConfig config) {
		super(configuration, cache, logger, config);
		status = new TriggerStatus(
				"Run by user", true
		);	
	}

	@Override
	public void checkTrigger() {}
	@Override
	public void wasTriggered(LaunchAgent launch) {
		launch.setMode(LaunchMode.USER);
	}
}
