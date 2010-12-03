package trigger;

import java.util.Date;

import core.Application;
import core.Cache;

import lifecycle.LaunchManager.TriggerStatus;


import data.AbstractTrigger;

public class IntervallTrigger extends AbstractTrigger {

	private enum Property { DATE };
	
	private IntervallTriggerConfig config;
	
	private Date newDate;
	
	public IntervallTrigger(IntervallTriggerConfig config) {
		super(config);
		this.config = config;
	}
	
	private void setLastDate(Date date){
		
		Cache cache = Application.getInstance().getCache();
		cache.addProperty(
				config.getId(), Property.DATE.toString(), ""+date.getTime()
		);
	}
	
	private Date getLastDate(){
		
		Cache cache = Application.getInstance().getCache();
		String value = cache.getProperty(
				config.getId(), Property.DATE.toString()
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
			return launcher.new TriggerStatus(config.getName()+" initial run", true);
		}else{
			if((lastDate.getTime() + config.getIntervall()) <= newDate.getTime()){
				return launcher.new TriggerStatus(
						config.getName()+" time elapsed", true
				);
			}else{
				return launcher.new TriggerStatus(
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
