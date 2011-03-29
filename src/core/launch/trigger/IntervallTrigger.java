package core.launch.trigger;

import java.util.Date;


import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.logger.Logger;


public class IntervallTrigger extends AbstractTrigger {

	private enum PROPERTY { DATE };
	
	private IntervallTriggerConfig config;
	
	private Date newDate;
	
	public IntervallTrigger(Configuration configuration, Cache cache, Logger logger, IntervallTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (IntervallTriggerConfig) super.config;
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
	public void checkTrigger() {
		
		Date lastDate = getLastDate();
		newDate = new Date();
		
		if(lastDate == null){
			status = new TriggerStatus(config.getName()+" initial run", true);
		}else{
			if((lastDate.getTime() + config.getIntervall()) <= newDate.getTime()){
				status = new TriggerStatus(
						config.getName()+" time elapsed", true
				);
			}else{
				status = new TriggerStatus(
						config.getName()+" time not elapsed", false
				);
			}
		}
	}

	@Override
	public void wasTriggered(LaunchAgent launch) {
		
		if(newDate != null){
			setLastDate(newDate);
		}
	}
}
