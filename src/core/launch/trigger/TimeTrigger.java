package core.launch.trigger;

import java.util.Calendar;
import java.util.Date;

import util.DateTools;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.logger.Logger;

public class TimeTrigger extends AbstractTrigger {

	private enum PROPERTY { DATE };
	
	private TimeTriggerConfig config;
	
	private Date newDate;
	
	public TimeTrigger(Configuration configuration, Cache cache, Logger logger, TimeTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (TimeTriggerConfig) super.config;
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
		
		if(!isScheduledToday(newDate)){
			return new TriggerStatus(
					"Not scheduled today", false
			);	
		}else if(wasRunningToday(lastDate, newDate)){
			return new TriggerStatus(
					"Already running today", false
			);	
		}else{
			if(isScheduledNow(newDate)){
				return new TriggerStatus(
						config.getName()+" scheduled", true
				);	
			}else{
				return new TriggerStatus(
						config.getName()+" not scheduled", false
				);	
			}
		}
	}

	private boolean isScheduledToday(Date currentDate) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2; // monday = 0
		return config.getDays()[currentDay];
	}
	
	private boolean wasRunningToday(Date lastDate, Date currentDate) {

		if(lastDate == null){
			return false;
		}else{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lastDate);
			int lastYear = calendar.get(Calendar.YEAR);
			int lastMonth = calendar.get(Calendar.MONTH);
			int lastDay = calendar.get(Calendar.DAY_OF_WEEK);
			calendar.setTime(currentDate);
			int currentYear = calendar.get(Calendar.YEAR);
			int currentMonth = calendar.get(Calendar.MONTH);
			int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
			
			return 
				(lastYear == currentYear) && 
				(lastMonth == currentMonth) && 
				(lastDay == currentDay);
		}
	}

	private boolean isScheduledNow(Date currentDate) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		long currentTime = 
			DateTools.hour2millis(calendar.get(Calendar.HOUR_OF_DAY)) + 
			DateTools.min2millis(calendar.get(Calendar.MINUTE));
		long startTime = config.getTime();
		long limit = config.getLimit();
		long endTime = startTime + limit;
		
		if(startTime != endTime){
			return (currentTime >= startTime) && (currentTime < endTime);
		}else{
			return currentTime >= startTime;
		}
	}
	
	@Override
	public void wasTriggered(boolean triggered) {
		
		if(triggered && newDate != null){
			setLastDate(newDate);
		}
	}
}
