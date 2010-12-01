package core;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

import repository.SVNClient;
import trigger.SVNTriggerConfig.OPTIONS;
import ui.OptionEditor;
import util.FileTools;
import util.IChangedListener;
import util.StringTools;
import util.UiTools;

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
public class Configuration implements IOptionInitializer {

	public enum GROUPS {
		GENERAL, NOTIFICATION, LOGGING
	}
	
	public enum OPTIONS {
		SCHEDULER, SCHEDULER_INTERVAL, MAXIMUM_AGENTS, MAXIMUM_HISTORY,
		NOTIFY, ADMINISTRATORS, SMTP_SERVER, SMTP_ADDRESS, VERBOSE
	}
	
	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "Configuration.xml";
	
	@SuppressWarnings("unused")
	private String version;
	
	private OptionContainer optionContainer;
	private ArrayList<LaunchConfig> launchConfigs;
	private transient ArrayList<IChangedListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public Configuration(String path){
		
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
				OPTIONS.NOTIFY.toString(), "Enable mail notification for application",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				GROUPS.NOTIFICATION.toString(),
				OPTIONS.ADMINISTRATORS.toString(), "mail-list of application admins (comma seperated)", 
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
		optionContainer.getOptions().add(new Option(
				GROUPS.LOGGING.toString(),
				OPTIONS.VERBOSE.toString(), "Verbose logging shows more details on console",
				Type.BOOLEAN, false
		));
		
		launchConfigs = new ArrayList<LaunchConfig>();
		listeners = new ArrayList<IChangedListener>();
		this.path = path;
		dirty = true;
	}
	
	@Override
	public void initOptions(OptionContainer container) {
		
//		OptionEditor.addNotificationTest(
//				container.getOption(OPTIONS.SMTP_SERVER.toString()),
//				new SMTPClient(Application.getInstance().getLogger())
//		);
		
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
	public int getSchedulerIntervall(){ 
		int min = optionContainer.getOption(OPTIONS.SCHEDULER_INTERVAL.toString()).getIntegerValue(); 
		if(min >= 0){
			return (int)StringTools.min2millis(min);
		}else{
			return 0;
		}
	}
	
	/** maximum number of parallel agents started by scheduled */
	public int getMaximumAgents(){ 
		return optionContainer.getOption(OPTIONS.MAXIMUM_AGENTS.toString()).getIntegerValue(); 
	}
	
	/** answers if logging is verbose */
	public boolean isVerbose(){
		return optionContainer.getOption(OPTIONS.VERBOSE.toString()).getBooleanValue();
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
	
	public static Configuration load(String path) throws Exception {
	
		Application.getInstance().getLogger().debug("load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.listeners = new ArrayList<IChangedListener>();
		configuration.path = path;
		for(LaunchConfig launchConfig : configuration.launchConfigs){
			launchConfig.setDirty(false);
		}
		configuration.dirty = false;
		return configuration;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			Application.getInstance().getLogger().debug("save: "+path);
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

	public void chekForSave() throws Exception {
		
		if(isDirty() && UiTools.confirmDialog("Save changes ?")){
			save();
		}
	}

	public String toHtml() {
		
		StringBuilder html = new StringBuilder();
		html.append("<html>");
		html.append("<hr><h1>"+Constants.APP_NAME+" -  Configuration</h1>");
		html.append("<hr><h2>Preferences</h2>");
		html.append(optionContainer.toHtml());
		for(LaunchConfig launchConfig : launchConfigs){
			html.append("<hr><h2>Launch ["+launchConfig.getName()+"]</h2>");
			html.append(launchConfig.getOptionContainer().toHtml());
			html.append("<h3>Operation(s):</h3>");
			html.append("<ol>");
			for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
				html.append("<li><b>["+operationConfig.getName()+"] Operation</b>");
				html.append(operationConfig.getOptionContainer().toHtml());
				html.append("</li>");
			}
			html.append("</ol>");
			html.append("<h3>Trigger(s):</h3>");
			html.append("<ol>");
			for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
				html.append("<li><b>["+triggerConfig.getName()+"] Trigger</b>");
				html.append(triggerConfig.getOptionContainer().toHtml());
				html.append("</li>");
			}
			html.append("</ol>");
		}
		html.append("<hr><p>"+Constants.APP_FULL_NAME+" - "+StringTools.getTextDate(new Date())+"</p><hr>");
		html.append("</html>");
		return html.toString();
	}
}
