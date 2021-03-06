package core.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


import ui.IStatusClient;
import ui.IStatusProvider;
import util.DateTools;
import util.FileTools;
import util.IChangeListener;
import util.IChangeable;
import core.ISystemComponent;
import core.launch.ILifecycleListener;
import core.launch.LaunchAgent;
import core.launch.LaunchConfig;
import core.launch.LifecycleObject;
import core.launch.data.StatusManager.Status;
import core.launch.trigger.UserTrigger;
import core.persistence.Configuration;
import core.runtime.logger.ILogProvider;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

/** maintains launches to be executed */
public class LaunchManager implements ISystemComponent, ILifecycleListener, IChangeable, IStatusProvider {

	private Configuration configuration;
	private FileManager fileManager;
	private Logger logger;
	
	private ArrayList<LaunchAgent> agents;
	private ArrayList<IChangeListener> listeners;
	private IStatusClient client;
	
	public LaunchManager(
			Configuration configuration, 
			FileManager fileManager, 
			Logger logger)
	{
		this.configuration = configuration;
		this.fileManager = fileManager;
		this.logger = logger;
		
		agents = new ArrayList<LaunchAgent>();
		listeners = new ArrayList<IChangeListener>();
		client = null;
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	@Override
	public void setStatusClient(IStatusClient client){ this.client = client; }
	@Override
	public void status(String text){
		if(client != null){
			client.status(text);
		}
	}

	@Override
	public void init() throws Exception {
		cleanupLegacyFolders();
	}

	@Override
	public void shutdown() throws Exception {
		for(int i=agents.size()-1; i>=0; i--){
			LaunchAgent agent = agents.get(i);
			agent.syncStop(agent.getOperations().size() * 1000);
		}
	}
	
	/** async run a launch */
	public LaunchStatus runLaunch(LaunchAgent agent) {
		
		synchronized(agents){
			if(isReady() || agent.getTrigger() instanceof UserTrigger){
				if(!isRunning(agent.getConfig().getId())){
					agents.add(agent);
					agent.addListener(this);
					agent.asyncRun(0, agent.getConfig().getTimeout());
					return new LaunchStatus("Launch started", true);
				}else{
					return new LaunchStatus("Already running", false);
				}
			}else{
				return new LaunchStatus("No agent available", false);
			}
		}
	}
	
	/** sync stop a launch */
	public void stopLaunch(String id) {
		
		LaunchAgent agent = getLaunch(id);
		if(agent != null){ 
			long timeout = agent.getOperations().size() * 1000;
			try{
				agent.syncStop(timeout);
				boolean stuck = false;
				synchronized(agents){
					if(agents.contains(agent)){
						agents.remove(agent);
						stuck = true;
					}
				}
				if(stuck){
					notifyListeners();
				}
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
	
	public boolean isBusy() {
		
		synchronized(agents){
			return agents.size() > 0;
		}
	}
	
	public boolean isReady() {
		
		synchronized(agents){
			return agents.size() < configuration.getMaximumAgents();
		}
	}
	
	public boolean isRunning(String id) {
		
		LaunchAgent agent = getLaunch(id);
		return agent != null;
	}
	
	private LaunchAgent getLaunch(String id) {
		
		synchronized(agents){
			for(LaunchAgent agent : agents){
				if(agent.getConfig().getId().equals(id)){
					return agent;
				}
			}
			return null;
		}
	}

	@Override
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle) {
		
		LaunchAgent agent = (LaunchAgent)object;
		String date = DateTools.getTextDate(new Date());
		if(lifecycle == Lifecycle.START){
			status("Launch ["+agent.getConfig().getName()+"] has STARTED: "+date);
		}
		if(lifecycle == Lifecycle.FINISH){
			status("Launch ["+agent.getConfig().getName()+"] has FINISHED: "+date);
			synchronized(agents){
				agents.remove(agent);
			}
		}
		notifyListeners();
	}
	
	public class LaunchStatus {
		
		public String message;
		public boolean launched;
		
		public LaunchStatus(String message, boolean launched){
			this.message = message;
			this.launched = launched;
		}
	}
	
	public class LaunchInfo implements Comparable<LaunchInfo> {
		
		public String name;
		public String id;
		public String trigger;
		public Date start;
		public int progress;
		public Date proof;
		public Status status;
		
		public LaunchInfo(LaunchAgent agent){
			name = agent.getConfig().getName();
			id = agent.getConfig().getId();
			trigger = agent.getTrigger().getStatus().message;
			start = agent.getStatusManager().getStart();
			progress = agent.getStatusManager().getProgress();
			proof = agent.getProofOfLiveMonitor().getUpdated();
			status = agent.getStatusManager().getStatus();
		}
		
		@Override
		public int compareTo(LaunchInfo o) {
			return name.compareTo(o.name);
		}
	}
	
	public ArrayList<LaunchInfo> getLaunchInfo(){
		
		synchronized(agents){
			ArrayList<LaunchInfo> infos = new ArrayList<LaunchInfo>();
			for(LaunchAgent agent : agents){
				infos.add(new LaunchInfo(agent));
			}
			Collections.sort(infos);
			return infos;
		}
	}
	
	public ILogProvider getLoggingProvider(String id){
		
		LaunchAgent agent = getLaunch(id);
		if(agent != null){
			return agent.getLogger();
		}else{
			return null;
		}
	}
	
	private void cleanupLegacyFolders() {
		
		final ArrayList<File> legacyFiles = getLegacyFolders();
		if(legacyFiles.size() > 0){
			Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					for(File file : legacyFiles){
						logger.debug(Module.COMMON, "cleanup: "+file.getAbsolutePath());
						try{
							if(file.isDirectory()){
								FileTools.deleteFolder(file.getAbsolutePath());
							}else{
								FileTools.deleteFile(file.getAbsolutePath());
							}
						}catch(Exception e){
							logger.error(Module.COMMON, e);
						}
					}
				}
			});
			thread.start();
		}
	}
	
	private ArrayList<File> getLegacyFolders() {
		
		ArrayList<File> folders = new ArrayList<File>();
		for(File file : fileManager.getBuildFolder().listFiles()){
			boolean legacy = true;
			if(file.isDirectory()){
				for(LaunchConfig launchConfig : configuration.getLaunchConfigs()){
					if(launchConfig.getId().equals(file.getName())){
						legacy = false;
						break;
					}
				}
			}
			if(legacy){
				folders.add(file);
			}
		}
		return folders;
	}
}
