package core.runtime;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import util.IChangeListener;
import util.IChangeable;
import util.Task;

import core.ISystemComponent;
import core.launch.LaunchAgent;
import core.launch.LaunchConfig;
import core.launch.trigger.AbstractTrigger;
import core.launch.trigger.AbstractTriggerConfig;
import core.launch.trigger.AbstractTrigger.TriggerStatus;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.LaunchManager.LaunchStatus;
import core.runtime.confluence.IConfluenceClient;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ErrorManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;


/** checks the configured launches for triggers to be fired */
public class ScheduleManager implements ISystemComponent, IChangeable {

	public static final String SCHEDULER_TASK_NAME = "SchedulerTask";
	
	public enum Priority {
		HIGH, NORMAL, LOW
	}
	
	private ErrorManager errorManager;
	private Configuration configuration;
	private Cache cache;
	private History history;
	private FileManager fileManager;
	private TaskManager taskManager;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private IConfluenceClient confluenceClient;
	private LaunchManager launchManager;
	private Logger logger;
	private SchedulerTask scheduler;
	private Date updated;
	private ArrayList<IChangeListener> listeners;
	
	public ScheduleManager(
			ErrorManager errorManager,
			Configuration configuration,
			Cache cache,
			History history,
			FileManager fileManager,
			TaskManager taskManager,
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			IConfluenceClient confluenceClient,
			LaunchManager launchManager, 
			Logger logger
	){
		this.errorManager = errorManager;
		this.configuration = configuration;
		this.cache = cache;
		this.history = history;
		this.fileManager = fileManager;
		this.taskManager = taskManager;
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.confluenceClient = confluenceClient;
		this.launchManager = launchManager;
		this.logger = logger;
		scheduler = null;
		updated = null;
		listeners = new ArrayList<IChangeListener>();
	}
	
	@Override
	public void init() throws Exception {
		if(configuration.isScheduler()){
			startScheduler(60 * 1000); // 1 min delay
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		stopScheduler();
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public boolean isRunning(){
			return scheduler != null;
	}
	
	/** run cyclic scheduler */
	public void startScheduler(long delay) throws Exception { 
		if(scheduler == null){
			scheduler = new SchedulerTask(true);
			scheduler.asyncRun(delay, 0); 
			notifyListeners();
		}
	}
	
	/** stop cyclic scheduler */
	public void stopScheduler() throws Exception { 
		if(scheduler != null){
			scheduler.syncStop(1000);
			scheduler = null;
			notifyListeners();
		}
	}
	
	/** run scheduler once */
	public void triggerScheduler(long delay) {

		SchedulerTask scheduler = new SchedulerTask(false);
		scheduler.asyncRun(delay, 0);
	}
	
	private synchronized void checkSchedules() {
		
		ArrayList<LaunchConfig> launchConfigs = getSortedLaunchConfigs();
		logger.debug(Module.COMMON, "Scheduler START => checking "+launchConfigs.size()+" launches");
		int triggered = 0;
		for(LaunchConfig launchConfig : launchConfigs){
			if(launchManager.isReady()){
				logger.log(Module.COMMON, "Check Launch ["+launchConfig.getName()+"]");
				if(checkSchedules(launchConfig)){
					triggered++;
				}
			}else{
				break;
			}
		}
		logger.debug(Module.COMMON, "Scheduler STOP => "+triggered+" launches triggered");
	}
	
	private boolean checkSchedules(LaunchConfig launchConfig) {
		
		for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
			if(triggerConfig.isReady()){
				logger.debug(
						Module.COMMON, 
						"Check Trigger ["+triggerConfig.getName()+"]"
				);
				AbstractTrigger trigger = triggerConfig.createTrigger(
						configuration, cache, taskManager, logger
				);
				trigger.checkTrigger();
				TriggerStatus triggerStatus = trigger.getStatus();
				if(triggerStatus.triggered){

					LaunchAgent launch = launchConfig.createLaunch(
							errorManager, configuration, cache, history, fileManager, 
							taskManager, smtpClient, httpServer, confluenceClient, trigger
					);
					LaunchStatus launchStatus = launchManager.runLaunch(launch);
					if(launchStatus.launched){
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] is TRIGGERD: "+triggerStatus.message
						);
						trigger.wasTriggered(launch);
						return true;
					}else{
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] is BLOCKED: "+launchStatus.message
						);
						return false;
					}

				}else{
					logger.debug(
							Module.COMMON, 
							"Trigger ["+triggerConfig.getName()+"] is IDLE: "+triggerStatus.message
					);
				}
			}
		}
		return false;
	}
	
	/** get randomized priority list of the launch-configurations to be scheduled */
	private ArrayList<LaunchConfig> getSortedLaunchConfigs(){
		
		ArrayList<LaunchConfig> launchConfigs = new ArrayList<LaunchConfig>();
		for(LaunchConfig launchConfig : configuration.getLaunchConfigs()){
			if(launchConfig.isReady()){
				launchConfigs.add(launchConfig);
			}
		}
		Collections.shuffle(launchConfigs);
		Collections.sort(launchConfigs, new Comparator<LaunchConfig>(){
			  @Override public int compare(LaunchConfig c1, LaunchConfig c2) { // higher priority at start of list
				  if(getPriorityValue(c1.getPriority()) > getPriorityValue(c2.getPriority())){
					  return -1;
				  }else if(getPriorityValue(c1.getPriority()) < getPriorityValue(c2.getPriority())){
					  return 1;
				  }else{
					  return 0;
				  }
			  }
		});
		return launchConfigs;
	}
	
	public void setUpdated(Date updated){
		synchronized(this){
			this.updated = updated;
		}
		notifyListeners();
	}
	
	public Date getUpdated(){ 
		synchronized(this){
			return updated;
		}
	}
	
	private class SchedulerTask extends Task {
		
		private boolean cyclic;
		
		public SchedulerTask(boolean cyclic){
			super(SCHEDULER_TASK_NAME, taskManager);
			this.cyclic = cyclic;
			updateCycle();
		}
		
		private void updateCycle() {
			if(cyclic){
				setCyclic(configuration.getSchedulerIntervall());
			}
		}

		@Override
		protected void runTask() {
			if(!configuration.getMaintenanceConfig().isMaintenanceToday(new Date())){
				checkSchedules();
			}else{
				logger.debug(Module.COMMON, "Scheduler IDLE for Maintenance");
			}
			setUpdated(new Date());
			updateCycle();
		}
	}
	
	public static int getPriorityValue(Priority priority){
		
		if(priority == Priority.LOW){
			return 0;
		}else if(priority == Priority.NORMAL){
			return 1;
		}else if(priority == Priority.HIGH){
			return 2;
		}else {
			return -1;
		}
	}
}
