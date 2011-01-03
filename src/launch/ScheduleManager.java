package launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import util.IChangedListener;
import util.Task;

import core.Cache;
import core.Configuration;
import core.FileManager;
import core.History;
import core.ISystemComponent;
import core.TaskManager;
import data.AbstractTrigger;
import data.AbstractTriggerConfig;
import data.LaunchConfig;
import data.AbstractTrigger.TriggerStatus;
import launch.LaunchManager.LaunchStatus;

import logger.Logger;
import logger.ILogConfig.Module;
import mail.SmtpManager;

/** checks the configured launches for triggers to be fired */
public class ScheduleManager implements ISystemComponent {

	private Configuration configuration;
	private Cache cache;
	private History history;
	private FileManager fileManager;
	private TaskManager taskManager;
	private SmtpManager smtpManager;
	private LaunchManager launchManager;
	private Logger logger;
	private SchedulerTask scheduler;
	private Date updated;
	private ArrayList<IChangedListener> listeners;

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
	
	public ScheduleManager(
			Configuration configuration,
			Cache cache,
			History history,
			FileManager fileManager,
			TaskManager taskManager,
			SmtpManager smtpManager,
			LaunchManager launchManager, 
			Logger logger
	){
		
		this.configuration = configuration;
		this.cache = cache;
		this.history = history;
		this.fileManager = fileManager;
		this.taskManager = taskManager;
		this.smtpManager = smtpManager;
		this.launchManager = launchManager;
		this.logger = logger;
		scheduler = null;
		updated = null;
		listeners = new ArrayList<IChangedListener>();
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	@Override
	public void init() throws Exception {
		if(configuration.isScheduler()){
			startScheduler(SchedulerTask.DELAY);
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		stopScheduler();
	}
	
	/** run cyclic scheduler */
	public void startScheduler(long delay){ 
		if(scheduler == null){
			scheduler = new SchedulerTask(true);
			scheduler.asyncRun(delay, SchedulerTask.TIMEOUT); 
		}
	}
	
	/** stop cyclic scheduler */
	public void stopScheduler(){ 
		if(scheduler != null){
			scheduler.syncKill();
			scheduler = null;
		}
	}
	
	/** run scheduler once */
	public void triggerScheduler(long delay) {

		SchedulerTask scheduler = new SchedulerTask(false);
		scheduler.asyncRun(delay, SchedulerTask.TIMEOUT);
	}
	
	public synchronized void checkSchedules() {
		
		logger.debug(Module.COMMON, "Checking schedules");
		ArrayList<LaunchConfig> launchConfigs = getRandomizedLaunches();
		for(LaunchConfig launchConfig : launchConfigs){
			if(launchManager.isReady()){
				checkSchedules(launchConfig);
			}else{
				break;
			}
		}
		setUpdated(new Date());
	}
	
	private void checkSchedules(LaunchConfig launchConfig) {
		
		boolean launched = false;
		for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
			AbstractTrigger trigger = triggerConfig.createTrigger(
					configuration, cache, taskManager, logger
			);
			TriggerStatus triggerStatus = trigger.isTriggered();
			if(triggerStatus.triggered){
				if(!launched)
				{
					LaunchAgent launch = launchConfig.createLaunch(
							configuration, cache, history, fileManager, taskManager, smtpManager, triggerStatus.message
					);
					LaunchStatus launchStatus = launchManager.runLaunch(launch);
					if(launchStatus.launched){
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] triggered: "+triggerStatus.message
						);
						trigger.wasTriggered(true);
						launched = true;
					}else{
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] aborded: "+launchStatus.message
						);
						trigger.wasTriggered(false); // actual launched
						break;
					}
				}else{
					trigger.wasTriggered(true); // simulate launched
				}
			}else{
				logger.debug(
						Module.COMMON, 
						"Trigger ["+triggerConfig.getId()+"] idle: "+triggerStatus.message
				);
			}
		}
	}
	
	private ArrayList<LaunchConfig> getRandomizedLaunches(){
		
		ArrayList<LaunchConfig> configs = new ArrayList<LaunchConfig>();
		for(LaunchConfig config : configuration.getLaunchConfigs()){
			if(config.isReady()){
				configs.add(config);
			}
		}
		Collections.shuffle(configs);
		return configs;
	}
	
	private class SchedulerTask extends Task {
		
		public static final long DELAY = 60 * 1000; // 1min
		public static final long TIMEOUT = 60 * 60 * 1000; // 1h
		
		private boolean cyclic;
		
		public SchedulerTask(boolean cyclic){
			super("SchedulerTask", taskManager);
			this.cyclic = cyclic;
			setCycle();
		}
		
		private void setCycle() {
			if(cyclic){
				setCycle(configuration.getSchedulerIntervall());
			}
		}

		@Override
		protected void runTask() {
			checkSchedules();
			setCycle();
		}
	}
}
