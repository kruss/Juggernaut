package core.launch.trigger;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;

public class UserTriggerConfig extends AbstractTriggerConfig {

	@Override
	public String getName(){ return "User"; }
	@Override
	public String getDescription(){ return "Generic User-Trigger"; }
	@Override
	public boolean isValid(){ return true; }
	
	@Override
	public AbstractTrigger createTrigger(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			Logger logger)
	{
		return new UserTrigger(configuration, cache, logger, this);
	}
}
