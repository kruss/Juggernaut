package trigger;

import java.util.Date;
import java.util.HashMap;

import lifecycle.LaunchManager;
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
		
		HashMap<String, String> cache = launcher.getCache();
		cache.put(
				config.getId()+"::"+Property.DATE.toString(), ""+date.getTime()
		);
	}
	
	private Date getLastDate(){
		
		HashMap<String, String> cache = launcher.getCache();
		String value = cache.get(config.getId()+"::"+Property.DATE.toString());
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
			return LaunchManager.INITIAL_TRIGGER;
		}else{
			if((lastDate.getTime() + config.getIntervall()) <= newDate.getTime()){
				return launcher.new TriggerStatus(
						"Intervall exceeded", true
				);
			}else{
				return launcher.new TriggerStatus(
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
