package core.persistence;


import java.io.File;
import java.util.ArrayList;



import ui.dialog.OptionEditorDialog;
import ui.dialog.PropertyInfo;
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
import core.launch.data.property.Property;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.runtime.FileManager;
import core.runtime.IToolConfig;
import core.runtime.MaintenanceConfig;
import core.runtime.TaskManager;
import core.runtime.confluence.ConfluenceClient;
import core.runtime.confluence.ConfluenceTest;
import core.runtime.confluence.IConfluenceClient;
import core.runtime.confluence.IConfluenceConfig;
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
	IConfluenceConfig,
	IChangeable
{

	public static Configuration create(Cache cache, TaskManager taskManager, FileManager fileManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			return Configuration.load(cache, taskManager, fileManager, logger, file.getAbsolutePath());
		}else{
			return new Configuration(cache, fileManager, logger, file.getAbsolutePath());
		}	
	}
	
	public enum GROUPS {
		GENERAL, HTTP, SMTP, CONFLUENCE, RUNTIME, TOOL
	}
	
	public enum OPTIONS {
		SCHEDULER, SCHEDULER_INTERVAL, MAXIMUM_AGENTS, MAXIMUM_HISTORY, SERVER, HTTP_PORT, 
		NOTIFICATION, ADMINISTRATORS, SMTP_SERVER, HOST_ADDRESS, CONFLUENCE_SERVER,
		PROPERTY, UNLOCKER
	}
	
	public static final String OUTPUT_FILE = "Configuration.xml";
	
	private transient Cache cache;
	private transient FileManager fileManager;
	private transient Logger logger;
	private transient ArrayList<IChangeListener> listeners;
	private transient String path;
	private transient boolean dirty;
	
	private String version;
	private OptionContainer optionContainer;
	private ArrayList<LaunchConfig> launchConfigs;
	private MaintenanceConfig maintenanceConfig;
	private LogConfig logConfig;

	public Configuration( 
			Cache cache,
			FileManager fileManager, 
			Logger logger, 
			String path)
	{
		this.cache = cache;
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
				GROUPS.HTTP.toString(),
				OPTIONS.SERVER.toString(), "Run the HTTP-Server",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.HTTP.toString(),
				OPTIONS.HTTP_PORT.toString(), "The HTTP-Server port", 
				Type.INTEGER, 80, 1, 1024
		));
		optionContainer.setOption(new Option(
				GROUPS.SMTP.toString(),
				OPTIONS.NOTIFICATION.toString(), "Perform eMail notifications",
				Type.BOOLEAN, false
		));
		optionContainer.setOption(new Option(
				GROUPS.SMTP.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "List of administrator eMails (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SMTP.toString(),
				OPTIONS.SMTP_SERVER.toString(), "The SMTP-Server for notifications", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SMTP.toString(),
				OPTIONS.HOST_ADDRESS.toString(), "The HOST-Address for notifications", 
				Type.TEXT_SMALL, "SMTP@"+Constants.APP_NAME
		));
		optionContainer.setOption(new Option(
				GROUPS.CONFLUENCE.toString(),
				OPTIONS.CONFLUENCE_SERVER.toString(), "The Confluence-Server for updates (user/pwd: "+IConfluenceClient.RPC_USER+")", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.RUNTIME.toString(),
				OPTIONS.PROPERTY.toString(), "System-Properties (linewise <key=value>, commented with '//')",
				Type.TEXT_AREA, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.TOOL.toString(),
				OPTIONS.UNLOCKER.toString(), "Command receiving locked ressource-path as argument", 
				Type.TEXT, ""
		));
		
		launchConfigs = new ArrayList<LaunchConfig>();
		maintenanceConfig = new MaintenanceConfig();
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
	public void shutdown() throws Exception {
		cache.cleanup(this);
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.SCHEDULER.toString()),
				new OptionEditorDialog("Advanced", maintenanceConfig.getOptionContainer(), new IChangeListener(){
					@Override
					public void changed(Object object) {
							maintenanceConfig.setDirty(true);
							notifyListeners();
					} 
				})
		);
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.HTTP_PORT.toString()),
				new HttpTest(this, logger)
		);
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.SMTP_SERVER.toString()),
				new SmtpTest(new SmtpClient(this), logger)
		);
		
		OptionEditor.setOptionDelegate(
				container.getOption(OPTIONS.CONFLUENCE_SERVER.toString()),
				new ConfluenceTest(new ConfluenceClient(), logger)
		);
	}
	@Override
	public void initEditor(OptionEditor editor) {
		
		ArrayList<String> keys = new ArrayList<String>();
		for(Property property : getSystemProperties()){
			keys.add(property.key);
		}
		PropertyInfo info = new PropertyInfo(Property.GENERIC_ID, keys);
		info.setInfo(editor);
	}
	
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
	public String getHostAddress() {
		return optionContainer.getOption(OPTIONS.HOST_ADDRESS.toString()).getStringValue();
	}
	
	@Override
	public ArrayList<String> getAdministratorAddresses() {
		
		String value = optionContainer.getOption(OPTIONS.ADMINISTRATORS.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	@Override
	public String getConfluenceServer() {
		return optionContainer.getOption(OPTIONS.CONFLUENCE_SERVER.toString()).getStringValue();
	}
	
	public ArrayList<Property> getSystemProperties() {
		
		String value = optionContainer.getOption(OPTIONS.PROPERTY.toString()).getStringValue();
		ArrayList<String> entries = StringTools.split(value, "\\n", "//");
		ArrayList<Property> properties = new ArrayList<Property>();
		for(String entry : entries){
			String[] seg = entry.split("=");
			if(seg.length == 2){
				Property property = new Property(Property.GENERIC_ID, seg[0].toUpperCase(), seg[1]);
				properties.add(property);
			}
		}
		return properties;
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
	public MaintenanceConfig getMaintenanceConfig(){ return maintenanceConfig; }
	public LogConfig getLogConfig() { return logConfig; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ 
		if(dirty){ 
			return true; 
		}else if(maintenanceConfig.isDirty() || logConfig.isDirty()){
			return true;
		}else{
			for(LaunchConfig launchConfig : launchConfigs){
				if(launchConfig.isDirty()){ return true; }
			}
		}
		return false;
	}
	
	public static Configuration load(
			Cache cache, TaskManager taskManager, FileManager fileManager, Logger logger, String path
	) throws Exception {
	
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.cache = cache;
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
				triggerConfig.initInstance(cache, taskManager, logger);
			}
		}
		configuration.dirty = false;
		configuration.maintenanceConfig.setDirty(false);
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
			maintenanceConfig.setDirty(false);
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
					html.append("<li><b>[ "+operationConfig.getUIName()+" ]</b>");
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
		html.append("<h2>Maintenance</h2>");
		html.append(maintenanceConfig.getOptionContainer().toHtml());
		html.append("<h2>Logging</h2>");
		html.append(logConfig.getOptionContainer().toHtml());
		return html.toString();
	}
}
