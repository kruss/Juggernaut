package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JTextField;

import logger.ILogConfig;
import logger.Logger;

import smtp.ISmtpConfig;
import util.DateTools;
import util.FileTools;
import util.IChangedListener;
import util.StringTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import data.AbstractOperationConfig;
import data.AbstractTriggerConfig;
import data.IOptionInitializer;
import data.LaunchConfig;
import data.Option;
import data.OptionContainer;
import data.Option.Type;

/**
 * the configuration of the application,- will be serialized
 */
public class Configuration 
implements 
	ISystemComponent, 
	IOptionInitializer, 
	ILogConfig,
	ISmtpConfig
{

	public static Configuration create(FileManager fileManager, TaskManager taskManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+Configuration.OUTPUT_FILE);
		if(file.isFile()){
			return Configuration.load(taskManager, logger, file.getAbsolutePath());
		}else{
			return new Configuration(logger, file.getAbsolutePath());
		}	
	}
	
	public enum GROUPS {
		GENERAL, NOTIFICATION, LOGGING
	}
	
	public enum OPTIONS {
		SCHEDULER, SCHEDULER_INTERVAL, MAXIMUM_AGENTS, MAXIMUM_HISTORY,
		NOTIFICATION, ADMINISTRATORS, SMTP_SERVER, SMTP_ADDRESS, LOGGING
	}
	
	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "Configuration.xml";
	
	private transient Logger logger;
	
	@SuppressWarnings("unused")
	private String version;
	private OptionContainer optionContainer;
	private ArrayList<LaunchConfig> launchConfigs;
	private transient ArrayList<IChangedListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public Configuration(Logger logger, String path){
		
		this.logger = logger;
		
		version = Constants.APP_VERSION;
		optionContainer = new OptionContainer();
		optionContainer.setDescription("The application preferences");
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.SCHEDULER.toString(), "Run the launch scheduler",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.SCHEDULER_INTERVAL.toString(), "The scheduler intervall in minutes", 
				Type.INTEGER, 5, 1, 180
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.MAXIMUM_AGENTS.toString(), "Maximum number of parallel launches", 
				Type.INTEGER, 3, 1, 10
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.GENERAL.toString(),
				OPTIONS.MAXIMUM_HISTORY.toString(), "Maximum number of launches in history (0 = unlimited)", 
				Type.INTEGER, 1000, 0, 1000
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.NOTIFICATION.toString(), "The application's notification-mode",
				Type.TEXT_LIST, StringTools.enum2strings(NotificationMode.class), NotificationMode.DISABLED.toString()
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "email-list of application admins (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.SMTP_SERVER.toString(), "The SMTP-Server for notifications", 
				Type.TEXT_SMALL, ""
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.SMTP_ADDRESS.toString(), "The SMTP-Address for notifications", 
				Type.TEXT_SMALL, "SMTP@"+Constants.APP_NAME
		));
		for(Module module : Module.values()){
			optionContainer.getOptions().add(new Option(
					GROUPS.LOGGING.toString(),
					module.toString()+"_"+OPTIONS.LOGGING, "Log-Level for "+module.toString()+" module",
					Type.TEXT_LIST, StringTools.enum2strings(Level.class), Level.NORMAL.toString()
			));
		}
		
		launchConfigs = new ArrayList<LaunchConfig>();
		listeners = new ArrayList<IChangedListener>();
		this.path = path;
		dirty = true;
	}
	
	@Override
	public void init() throws Exception {
		save();
		logger.setConfig(this);
	}

	@Override
	public void shutdown() throws Exception {}
	
	@Override
	public void initOptions(OptionContainer container) {
		
		// TODO temp...
		final Option smtpServer = container.getOption(OPTIONS.SMTP_SERVER.toString());
		JMenuItem testConnection = new JMenuItem("Test");
		testConnection.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				System.out.println("test: "+((JTextField)smtpServer.component).getText());
			}
		});
		smtpServer.addPopup(testConnection);
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
	public Level getLogLevel(Module module){
		return Level.valueOf(optionContainer.getOption(module.toString()+"_"+OPTIONS.LOGGING).getStringValue());
	}
	
	@Override
	public NotificationMode getNotificationMode(){
		return NotificationMode.valueOf(optionContainer.getOption(OPTIONS.NOTIFICATION.toString()).getStringValue());
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
	public ArrayList<String> getAdministrators() {
		
		String value = optionContainer.getOption(OPTIONS.ADMINISTRATORS.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	public ArrayList<LaunchConfig> getLaunchConfigs(){ return launchConfigs; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	
	public boolean isDirty(){ 
		if(dirty){ return true; }
		for(LaunchConfig launchConfig : launchConfigs){
			if(launchConfig.isDirty()){ return true; }
		}
		return false;
	}
	
	public State getState(){ return isDirty() ? State.DIRTY : State.CLEAN; }
	
	public static Configuration load(TaskManager taskManager, Logger logger, String path) throws Exception {
	
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.logger = logger;
		configuration.listeners = new ArrayList<IChangedListener>();
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
		return html.toString();
	}
}
