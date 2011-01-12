package trigger;

import java.util.Date;

import logger.Logger;

import core.Cache;
import core.Configuration;

import data.AbstractTrigger;

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
	public TriggerStatus isTriggered() {
		
		Date lastDate = getLastDate();
		newDate = new Date();
		
		if(lastDate == null){
			return new TriggerStatus(config.getName()+" initial run", true);
		}else{
			if((lastDate.getTime() + config.getIntervall()) <= newDate.getTime()){
				return new TriggerStatus(
						config.getName()+" time elapsed", true
				);
			}else{
				return new TriggerStatus(
						config.getName()+" time not elapsed", false
				);
			}
		}
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(triggered && newDate != null){
			setLastDate(newDate);
		}
	}
}
