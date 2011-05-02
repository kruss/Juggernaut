package core.runtime;

import java.util.ArrayList;
import java.util.Date;

import core.Constants;
import core.ISystemComponent;
import core.persistence.Configuration;
import core.runtime.LaunchManager.LaunchInfo;
import core.runtime.TaskManager.TaskInfo;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;
import core.runtime.smtp.Mail;
import util.DateTools;
import util.Task;

public class WatchDog implements ISystemComponent {

	private static final long WATCH_DOG_CYCLE = 30 * 60 * 1000; // 30 min
	
	private Configuration configuration;
	private TaskManager taskManager;
	private ISmtpClient smtpClient;
	private ScheduleManager scheduleManager;
	private LaunchManager launchManager;
	private Logger logger;
	
	private WatchDogTask task;
	
	public WatchDog(
			Configuration configuration,
			TaskManager taskManager, 
			ISmtpClient smtpClient, 
			ScheduleManager scheduleManager,
			LaunchManager launchManager,
			Logger logger) 
	{
		this.configuration = configuration;
		this.taskManager = taskManager;
		this.smtpClient = smtpClient;
		this.scheduleManager = scheduleManager;
		this.launchManager = launchManager;
		this.logger = logger;
		task = null;
	}
	
	@Override
	public void init() throws Exception {
		if(task == null){
			task = new WatchDogTask(WATCH_DOG_CYCLE);
			task.asyncRun(WATCH_DOG_CYCLE, 0);
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		if(task != null){
			task.syncStop(1000);
			task = null;
		}
	}
	
	private void performChecks(){
		
		logger.debug(Module.COMMON, "WatchDog START");
		checkScheduler();
		checkLaunches();
		logger.debug(Module.COMMON, "WatchDog STOP");
	}
	
	private void checkScheduler() {

		logger.debug(Module.COMMON, "WatchDog: check Scheduler");
		if(configuration.isScheduler()){
			boolean found = false;
			ArrayList<TaskInfo> tasks = taskManager.getInfo();
			for(TaskInfo task : tasks){
				if(task.name.equals(ScheduleManager.SCHEDULER_TASK_NAME)){
					found = true;
					break;
				}
			}
			if(!found){
				performNotification("Scheduler-Task is dead => trying to restart");
				logger.log(Module.COMMON, "WatchDog: restart Scheduler");
				try{
					if(scheduleManager.isRunning()){
						scheduleManager.stopScheduler();
					}
					scheduleManager.startScheduler(0);
				}catch (Exception e){
					logger.error(Module.COMMON, e);
				}
			}
		}	
	}
	
	private void checkLaunches() {
		
		logger.debug(Module.COMMON, "WatchDog: check Launches");
		ArrayList<LaunchInfo> launches = launchManager.getLaunchInfo();
		Date date = new Date();
		for(LaunchInfo launch : launches){
			long time = date.getTime() - launch.proof.getTime();
			if(time > WATCH_DOG_CYCLE){
				performNotification("Launch ["+launch.name+"] is dead ("+DateTools.millis2min(time)+" min) => trying to stop");
				logger.log(Module.COMMON, "WatchDog: stop Launch ["+launch.name+"]");
				launchManager.stopLaunch(launch.id);
			}
		}
	}

	private void performNotification(String message){
		
		logger.error(Module.COMMON, "WatchDog: "+message);
		if(isNotificationEnabled()){
			Mail mail = new Mail("WatchDog");
			mail.from = smtpClient.getConfig().getHostAddress();
			mail.to = smtpClient.getConfig().getAdministratorAddresses();
			mail.content =
				"<font color=red><h1>"+Constants.APP_NAME+" WatchDog</h1>\n"+
				"<ul>\n"+
				"<li>Date: <b>"+DateTools.getTextDate(new Date())+"</b></li>\n"+
				"<li>Message: <b>"+message+"</b></li>\n"+
				"</ul>\n"+
				"<i>Please check system for consistency !</i></font>";
			try{ 
				smtpClient.send(mail, logger);
			}catch(Exception e){
				logger.error(Module.SMTP, e);
			}
		}
	}
	
	private boolean isNotificationEnabled() {
		return 
			smtpClient.isReady() && 
			smtpClient.getConfig().getAdministratorAddresses().size() > 0 && 
			configuration.isNotification();
	}

	private class WatchDogTask extends Task {
		
		public WatchDogTask(long cycle) {
			super("WatchDog", taskManager);
			this.setCyclic(cycle);
		}

		@Override
		protected void runTask() {
			try{
				performChecks();
			}catch (Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
}
