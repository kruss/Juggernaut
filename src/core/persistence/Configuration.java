package core.persistence;


import java.io.File;
import java.util.ArrayList;



import ui.option.IOptionInitializer;
import ui.option.Option;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import ui.option.Option.Type;
import util.DateTools;
import util.FileTools;
import util.IChangeListener;
import util.IChangeable;
import util.StringTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.Constants;
import core.ISystemComponent;
import core.launch.LaunchConfig;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.runtime.FileManager;
import core.runtime.IToolConfig;
import core.runtime.TaskManager;
import core.runtime.http.HttpTest;
import core.runtime.http.IHttpConfig;
import core.runtime.logger.LogConfig;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpConfig;
import core.runtime.smtp.SmtpClient;
import core.runtime.smtp.SmtpTest;


/**
 * the configuration of the application,- will be serialized
 */
public class Configuration 
implements 
	ISystemComponent, 
	IOptionInitializer, 
	IToolConfig,
	ISmtpConfig,
	IHttpConfig,
	IChangeable
{

	public static Configuration create(TaskManager taskManager, FileManager fileManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			return Configuration.load(taskManager, fileManager, logger, file.getAbsolutePath());
		}else{
			return new Configuration(fileManager, logger, file.getAbsolutePath());
		}	
	}
	
	public enum GROUPS {
		GENERAL, NOTIFICATION, EXTRA
	}
	
	public enum OPTIONS {
		SCHEDULER, SCHEDULER_INTERVAL, MAXIMUM_AGENTS, MAXIMUM_HISTORY, SERVER, HTTP_PORT, 
		NOTIFICATION, ADMINISTRATORS, SMTP_SERVER, SMTP_ADDRESS, 
		UNLOCKER
	}
	
	public static final String OUTPUT_FILE = "Configuration.xml";
	
	private transient FileManager fileManager;
	private transient Logger logger;
	private transient ArrayList<IChangeListener> listeners;
	private transient String path;
	private transient boolean dirty;
	
	private String version;
	private OptionContainer optionContainer;
	private ArrayList<LaunchConfig> launchConfigs;
	private LogConfig logConfig;

	public Configuration( 
			FileManager fileManager, 
			Logger logger, 
			String path)
	{
		this.fileManager = fileManager;
		this.logger = logger;
		
		version = Constants.APP_VERSION;
		optionContainer = new OptionContainer();
		optionContainer.setDescription("The application preferences");
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.SCHEDULER.toString(), "Run the scheduler",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.SCHEDULER_INTERVAL.toString(), "The scheduler intervall in minutes", 
				Type.INTEGER, 5, 1, 180
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.MAXIMUM_AGENTS.toString(), "Maximum number of parallel launches", 
				Type.INTEGER, 2, 1, 10
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.MAXIMUM_HISTORY.toString(), "Maximum number of launches in history (0 = unlimited)", 
				Type.INTEGER, 100, 0, 1000
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.SERVER.toString(), "Run the HTTP-Server",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.HTTP_PORT.toString(), "The HTTP-Server port", 
				Type.INTEGER, 80, 1, 1024
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.NOTIFICATION.toString(), "Perform eMail-notifications",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "List of administrator eMails (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.SMTP_SERVER.toString(), "The SMTP-Server for notifications", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.SMTP_ADDRESS.toString(), "The SMTP-Address for notifications", 
				Type.TEXT_SMALL, "SMTP@"+Constants.APP_NAME
		));
		optionContainer.setOption(new Option(
				GROUPS.EXTRA.toString(),
				OPTIONS.UNLOCKER.toString(), "Command to free locked ressources", 
				Type.TEXT, ""
		));
		
		launchConfigs = new ArrayList<LaunchConfig>();
		logConfig = new LogConfig();
		listeners = new ArrayList<IChangeListener>();
		this.path = path;
		dirty = true;
	}
	
	@Override
	public void init() throws Exception {
		save();
		logger.setConfig(logConfig);
		fileManager.setConfig(this);
	}

	@Override
	public void shutdown() throws Exception {}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.HTTP_PORT.toString()),
				new HttpTest(this, logger)
		);
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.SMTP_SERVER.toString()),
				new SmtpTest(new SmtpClient(this), logger)
		);
	}
	@Override
	public void initEditor(OptionEditor editor) {}
	
	/** answers if scheduler is active */
	public boolean isScheduler(){
		return optionContainer.getOption(OPTIONS.SCHEDULER.toString()).getBooleanValue();
	}
	
	/** the scheduler-interval in millis */
	public long getSchedulerIntervall(){ 
		return DateTools.min2millis(optionContainer.getOption(OPTIONS.SCHEDULER_INTERVAL.toString()).getIntegerValue());
	}
	
	/** maximum number of parallel agents started by scheduled */
	public int getMaximumAgents(){ 
		return optionContainer.getOption(OPTIONS.MAXIMUM_AGENTS.toString()).getIntegerValue(); 
	}
	
	/** maximum number of history entries, or 0 if unlimited */
	public int getMaximumHistory() {
		return optionContainer.getOption(OPTIONS.MAXIMUM_HISTORY.toString()).getIntegerValue();
	}
	
	@Override
	public boolean isHttpServer(){
		return optionContainer.getOption(OPTIONS.SERVER.toString()).getBooleanValue();
	}
	
	@Override
	public int getHttpPort(){ 
		return optionContainer.getOption(OPTIONS.HTTP_PORT.toString()).getIntegerValue(); 
	}
	
	@Override
	public boolean isNotification(){
		return optionContainer.getOption(OPTIONS.NOTIFICATION.toString()).getBooleanValue();
	}
	
	@Override
	public String getSmtpServer() {
		return optionContainer.getOption(OPTIONS.SMTP_SERVER.toString()).getStringValue();
	}
	
	@Override
	public String getSmtpAddress() {
		return optionContainer.getOption(OPTIONS.SMTP_ADDRESS.toString()).getStringValue();
	}
	
	@Override
	public ArrayList<String> getAdministratorAddresses() {
		
		String value = optionContainer.getOption(OPTIONS.ADMINISTRATORS.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	@Override
	public String getUnlocker(){
		return optionContainer.getOption(OPTIONS.UNLOCKER.toString()).getStringValue();
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public String getVersion(){ return version; }
	public OptionContainer getOptionContainer(){ return optionContainer; }
	public ArrayList<LaunchConfig> getLaunchConfigs(){ return launchConfigs; }
	public LogConfig getLogConfig() { return logConfig; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ 
		if(dirty){ 
			return true; 
		}else if(logConfig.isDirty()){
			return true;
		}else{
			for(LaunchConfig launchConfig : launchConfigs){
				if(launchConfig.isDirty()){ return true; }
			}
		}
		return false;
	}
	
	public static Configuration load(
			TaskManager taskManager, FileManager fileManager, Logger logger, String path
	) throws Exception {
	
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.fileManager = fileManager;
		configuration.logger = logger;
		configuration.listeners = new ArrayList<IChangeListener>();
		configuration.path = path;
		for(LaunchConfig launchConfig : configuration.launchConfigs){
			launchConfig.setDirty(false);
			for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
				operationConfig.initInstance(taskManager, logger);
			}
			for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
				triggerConfig.initInstance(taskManager, logger);
			}
		}
		configuration.dirty = false;
		configuration.logConfig.setDirty(false);
		return configuration;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			logger.debug(Module.COMMON, "save: "+path);
			XStream xstream = new XStream(new DomDriver());
			String xml = xstream.toXML(this);
			FileTools.writeFile(path, xml, false);
			for(LaunchConfig launchConfig : launchConfigs){
				launchConfig.setDirty(false);
			}
			logConfig.setDirty(false);
			dirty = false;
			notifyListeners();
		}
	}

	public String toHtml() {
		
		StringBuilder html = new StringBuilder();
		html.append("<h2>Preferences</h2>");
		html.append(optionContainer.toHtml());
		for(LaunchConfig launchConfig : launchConfigs){
			html.append("<hr>");
			html.append("<h2>Launch [ "+launchConfig.getName()+" ]</h2>");
			html.append(launchConfig.getOptionContainer().toHtml());
			if(launchConfig.getOperationConfigs().size() > 0){
				html.append("<h3>Operation(s):</h3>");
				html.append("<ol>");
				for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
					html.append("<li><b>[ "+operationConfig.getName()+" ]</b>");
					html.append(operationConfig.getOptionContainer().toHtml());
					html.append("</li>");
				}
				html.append("</ol>");
			}
			if(launchConfig.getTriggerConfigs().size() > 0){
				html.append("<h3>Trigger(s):</h3>");
				html.append("<ol>");
				for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
					html.append("<li><b>[ "+triggerConfig.getName()+" ]</b>");
					html.append(triggerConfig.getOptionContainer().toHtml());
					html.append("</li>");
				}
				html.append("</ol>");
			}
		}
		html.append("<hr>");
		html.append("<h2>Logging</h2>");
		html.append(logConfig.getOptionContainer().toHtml());
		return html.toString();
	}
}
