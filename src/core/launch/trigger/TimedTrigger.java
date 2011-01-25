package core.launch.trigger;

import java.util.Date;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.logger.Logger;

public class TimedTrigger extends AbstractTrigger {

	private enum PROPERTY { DATE };
	
	private TimedTriggerConfig config;
	
	private Date newDate;
	
	public TimedTrigger(Configuration configuration, Cache cache, Logger logger, TimedTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (TimedTriggerConfig) super.config;
	}
	
	private void setLastDate(Date date){
		cache.setValue(
				config.getId(), PROPERTY.DATE.toString(), ""+date.getTime()
		);
	}
	
	private Date getLastDate(){
		String value = cache.getValue(
				config.getId(), PROPERTY.DATE.toString()
		);
		if(value != null){
			return new Date(new Long(value).longValue());
		}else{
			return null;
		}
	}

	@Override
	public TriggerStatus isTriggered() {
		// TODO Auto-generated method stub
		return new TriggerStatus(config.getName()+" NYI!", false);
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(triggered && newDate != null){
			setLastDate(newDate);
		}
	}
}
