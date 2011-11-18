package core;

import java.io.File;

import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.FileManager;
import core.runtime.HeapManager;
import core.runtime.HistoryIndex;
import core.runtime.LaunchManager;
import core.runtime.Registry;
import core.runtime.ScheduleManager;
import core.runtime.TaskManager;
import core.runtime.WatchDog;
import core.runtime.confluence.ConfluenceClient;
import core.runtime.http.HttpServer;
import core.runtime.logger.ErrorManager;
import core.runtime.logger.SystemLogger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.SmtpClient;
import ui.Window;
import ui.menu.HelpMenu;
import ui.menu.ProjectMenu;
import ui.menu.ToolsMenu;
import ui.panel.ConfigPanel;
import ui.panel.HistoryPanel;
import ui.panel.LoggerPanel;
import ui.panel.PreferencePanel;
import ui.panel.SchedulerPanel;
import util.SystemTools;
import util.UiTools;

public class Juggernaut extends AbstractSystem {
	
	private static final String LOCK_FILE = "lock";
	
	public static void main(String[] args){
		
		File lock = getLockFile();
		try{
			if(!lock.isFile()){
				setAppStyle(args);
				Juggernaut juggernaut = new Juggernaut();
				juggernaut.init(); 
			}else{
				throw new Exception("Locked at: "+lock.getAbsolutePath());
			}
		}catch(Exception e){
			if(e != ABORT_EXCEPTION){
				e.printStackTrace();
				UiTools.errorDialog("Error on STARTUP !!!", e);
			}else{
				lock.delete();
			}
			System.exit(Constants.PROCESS_NOK);
		}
	}
	
	private static File getLockFile(){
		return new File(SystemTools.getWorkingDir()+File.separator+LOCK_FILE);
	}

	private static void setAppStyle(String[] args) throws Exception {
		int style = UiTools.getStyle(Constants.APP_STYLE_DEFAULT);
		for(int i=0; i<args.length; i++){
			if(args[i].equals("-style") && i<args.length-1){
				style = (new Integer(args[i+1])).intValue();
				break;
			}
		}
		Constants.APP_STYLE = (style != -1) ? style : 0;
	}

	private Logging logging;
	private Core core;
	private Persistence persistence;
	private Runtime runtime;
	private UI ui;
	
	private Juggernaut() throws Exception {
		setMonitor(new SystemMonitor());
		
		logging = new Logging();
		add(logging);
		core = new Core();
		add(core);
		persistence = new Persistence();
		add(persistence);
		runtime = new Runtime();
		add(runtime);
		ui = new UI(this);
		add(ui);
	}
	
	@Override
	public void init() throws Exception {
		
		monitor.startProgress(this, Constants.APP_NAME+" - INIT");
		try{
			getLockFile().createNewFile();
			super.init();
		}finally{
			monitor.stopProgress();
		}
	}

	@Override
	public void shutdown() throws Exception {
		
		monitor.startProgress(this,  Constants.APP_NAME+" - SHUTDOWN");
		try{
			super.shutdown();
		}finally{
			getLockFile().delete();
			monitor.stopProgress();
		}
	}
	
	public void revert() throws Exception {
		
		if(
				ui.isInitialized() && 
				persistence.configuration.isDirty() &&
				!runtime.launchManager.isBusy()
		){
			logging.logger.emph(Module.COMMON, "revert");
			// shutdown
			ui.shutdown();
			 remove(ui);
			runtime.shutdown();
			 remove(runtime);
			persistence.shutdown();
			 remove(persistence);
			// init
			persistence = new Persistence();
			 add(persistence);
			 persistence.init();
			runtime = new Runtime();
			 add(runtime);
			 runtime.init();
			ui = new UI(this);
			 add(ui);
			 ui.init();
		}else{
			throw new Exception("Not valid state for revert");
		}
	}

	/** the application's logger */
	private class Logging extends AbstractSystem {
		
		public ErrorManager errorManager;
		public SystemLogger logger;
		
		public Logging() throws Exception {
			
			errorManager = new ErrorManager();
			add(errorManager);
			
			logger = new SystemLogger(errorManager);
			add(logger);
		}
	}
	
	/** io related components */
	private class Core extends AbstractSystem {
		
		public TaskManager taskManager;
		public FileManager fileManager;
		public HeapManager heapManager;
		
		public Core() throws Exception {
			
			taskManager = new TaskManager(
					logging.logger);
			add(taskManager);
			fileManager = new FileManager(
					taskManager);
			add(fileManager);
			heapManager = new HeapManager(
					taskManager, 
					logging.logger);
			add(heapManager);
		}
	}
	
	/** persistence related components */
	private class Persistence extends AbstractSystem {
		
		public Configuration configuration;
		public Cache cache;
		public History history;
		
		public Persistence() throws Exception {
			
			cache = Cache.create(
					core.fileManager, 
					logging.logger);
			add(cache);
			configuration = Configuration.create(
					cache,
					core.taskManager,
					core.fileManager, 
					logging.logger);
			add(configuration);
			history = History.create(
					configuration, 
					core.fileManager, 
					logging.logger);
			add(history);
		}
	}
	
	/** runtime related components */
	private class Runtime extends AbstractSystem {
		
		public Registry registry;
		public HistoryIndex index;
		public SmtpClient smtpClient;
		public LaunchManager launchManager;
		public ScheduleManager scheduleManager;
		public HttpServer httpServer;
		public ConfluenceClient confluenceClient;
		public WatchDog watchDog;
		
		public Runtime() throws Exception {
			
			registry = new Registry(
					persistence.cache,
					core.taskManager, 
					logging.logger);
			add(registry);
			index = new HistoryIndex(
					core.fileManager, 
					core.taskManager,
					persistence.history, 
					logging.logger);
			add(index);
			smtpClient = new SmtpClient(
					persistence.configuration);
			add(smtpClient);
			httpServer = new HttpServer(
					persistence.configuration, 
					core.fileManager, 
					core.taskManager, 
					logging.logger);
			add(httpServer);
			confluenceClient = new ConfluenceClient();
			add(confluenceClient);
			launchManager = new LaunchManager(
					persistence.configuration,
					core.fileManager,
					logging.logger);
			add(launchManager);
			scheduleManager = new ScheduleManager(
					logging.errorManager,
					persistence.configuration, 
					persistence.cache,
					persistence.history, 
					core.fileManager, 
					core.taskManager,
					smtpClient,
					httpServer,
					confluenceClient,
					launchManager, 
					logging.logger);
			add(scheduleManager);
			watchDog = new WatchDog(
					persistence.configuration,
					core.taskManager,
					smtpClient,
					scheduleManager,
					launchManager,
					logging.logger);
			add(watchDog);
		}
	}
	
	/** ui related componets */
	private class UI extends AbstractSystem {
		
		public ProjectMenu projectMenu;
		public ToolsMenu toolsMenu;
		public HelpMenu helpMenu;
		public ConfigPanel configPanel;
		public SchedulerPanel schedulerPanel;
		public HistoryPanel historyPanel;
		public PreferencePanel preferencePanel;
		public LoggerPanel loggerPanel;
		public Window window;
		
		public UI(Juggernaut juggernaut) throws Exception {
			
			projectMenu = new ProjectMenu(
					juggernaut, 
					persistence.configuration, 
					runtime.launchManager, 
					logging.logger);
			add(projectMenu);
			toolsMenu = new ToolsMenu(
					juggernaut,
					logging.errorManager,
					persistence.cache, 
					persistence.configuration,
					runtime.registry,
					core.taskManager,
					core.fileManager, 
					core.heapManager,
					runtime.launchManager,
					logging.logger);
			add(toolsMenu);
			helpMenu = new HelpMenu(core.fileManager);
			add(helpMenu);
			configPanel = new ConfigPanel(
					logging.errorManager,
					persistence.configuration, 
					persistence.cache,
					persistence.history, 
					core.fileManager, 
					core.taskManager, 
					runtime.smtpClient,
					runtime.httpServer,
					runtime.confluenceClient,
					runtime.launchManager, 
					runtime.registry,
					logging.logger);
			add(configPanel);
			schedulerPanel = new SchedulerPanel(
					runtime.launchManager, 
					runtime.scheduleManager, 
					logging.logger);
			add(schedulerPanel);
			historyPanel = new HistoryPanel(
					persistence.history, 
					logging.logger);
			add(historyPanel);
			preferencePanel = new PreferencePanel(
					persistence.configuration, 
					runtime.scheduleManager, 
					runtime.httpServer);
			add(preferencePanel);
			loggerPanel = new LoggerPanel(
					persistence.configuration, 
					logging.logger);
			add(loggerPanel);
			window = new Window(
					logging.logger, 
					logging.errorManager,
					core.taskManager, 
					core.heapManager, 
					persistence.configuration, 
					runtime.launchManager, 
					runtime.scheduleManager,
					runtime.httpServer,
					projectMenu, 
					toolsMenu,
					helpMenu,
					configPanel, 
					schedulerPanel, 
					historyPanel, 
					loggerPanel,
					preferencePanel);
			add(window);
		}
	}
}
