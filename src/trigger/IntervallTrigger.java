package trigger;

import java.util.Date;
import java.util.HashMap;

import lifecycle.LaunchManager;
import lifecycle.LaunchManager.TriggerStatus;


import core.Application;

import data.AbstractTrigger;

public class IntervallTrigger extends AbstractTrigger {

	private enum Property { DATE };
	
	private IntervallTriggerConfig config;
	private Application application;
	
	private Date lastDate;
	private Date newDate;
	
	public IntervallTrigger(IntervallTriggerConfig config) {
		super(config);
		this.config = config;
		application = Application.getInstance();
	}
	
	private void setLastDate(Date date){
		
		HashMap<String, String> cache = application.getLaunchManager().getCache();
		cache.put(
				config.getId()+"::"+Property.DATE.toString(), 
				""+date.getTime()
		);
	}
	
	private Date getLastDate(){
		
		HashMap<String, String> cache = application.getLaunchManager().getCache();
		String value = cache.get(config.getId()+"::"+Property.DATE.toString());
		if(value != null){
			return new Date(new Long(value).longValue());
		}else{
			return null;
		}
	}
	
	@Override
	public TriggerStatus isTriggered() {
		
		newDate = new Date();
		lastDate = getLastDate();
		
		if(lastDate == null){
			return LaunchManager.INITIAL_TRIGGER;
		}else{
			if(lastDate.getTime() + config.getIntervall() >= newDate.getTime()){
				return application.getLaunchManager().new TriggerStatus(
						"Intervall exceeded", true
				);
			}else{
				return application.getLaunchManager().new TriggerStatus(
						"Intervall not exceeded", false
				);
			}
		}
	}

	@Override
	public void wasTriggered(boolean triggered) {
		
		if(newDate != null){
			setLastDate(newDate);
		}
	}
}
