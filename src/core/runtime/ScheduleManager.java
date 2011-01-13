package core.runtime;


import java.util.ArrayList;
import java.util.Collections;
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
import core.runtime.http.IHttpServer;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;


/** checks the configured launches for triggers to be fired */
public class ScheduleManager implements ISystemComponent, IChangeable {

	private Configuration configuration;
	private Cache cache;
	private History history;
	private FileManager fileManager;
	private TaskManager taskManager;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchManager launchManager;
	private Logger logger;
	private SchedulerTask scheduler;
	private Date updated;
	private ArrayList<IChangeListener> listeners;
	
	public ScheduleManager(
			Configuration configuration,
			Cache cache,
			History history,
			FileManager fileManager,
			TaskManager taskManager,
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			LaunchManager launchManager, 
			Logger logger
	){
		
		this.configuration = configuration;
		this.cache = cache;
		this.history = history;
		this.fileManager = fileManager;
		this.taskManager = taskManager;
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.launchManager = launchManager;
		this.logger = logger;
		scheduler = null;
		updated = null;
		listeners = new ArrayList<IChangeListener>();
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
			scheduler.asyncRun(delay, SchedulerTask.TIMEOUT); 
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
		scheduler.asyncRun(delay, SchedulerTask.TIMEOUT);
	}
	
	private void checkSchedules() {
		
		ArrayList<LaunchConfig> launchConfigs = getRandomizedLaunches();
		logger.log(Module.COMMON, "Checking schedules ("+launchConfigs.size()+")");
		for(LaunchConfig launchConfig : launchConfigs){
			if(launchManager.isReady()){
				if(!checkSchedules(launchConfig)){
					logger.log(Module.COMMON, "Launch ["+launchConfig.getName()+"] idle");
				}
			}else{
				break;
			}
		}
		setUpdated(new Date());
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
	
	private boolean checkSchedules(LaunchConfig launchConfig) {
		
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
							configuration, cache, history, fileManager, taskManager, smtpClient, httpServer, triggerStatus.message
					);
					LaunchStatus launchStatus = launchManager.runLaunch(launch);
					if(launchStatus.launched){
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] trigger: "+triggerStatus.message
						);
						trigger.wasTriggered(true);
						launched = true;
					}else{
						logger.log(
								Module.COMMON, 
								"Launch ["+launchConfig.getName()+"] blocked: "+launchStatus.message
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
		return launched;
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
				setCyclic(configuration.getSchedulerIntervall());
			}
		}

		@Override
		protected void runTask() {
			checkSchedules();
			setCycle();
		}
	}
}
