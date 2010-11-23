package core;

import java.util.ArrayList;

import util.FileTools;
import util.IChangedListener;
import util.StringTools;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import data.LaunchConfig;
import data.Option;
import data.OptionContainer;
import data.Option.Type;

/**
 * the configuration of the application,- will be serialized
 */
public class Configuration {

	public enum OPTIONS {
		SCHEDULER, SCHEDULER_INTERVALL, MAXIMUM_AGENTS, 
		NOTIFY, ADMINISTRATORS, SMTP_SERVER, SMTP_USER, SMTP_ADDRESS,
		VERBOSE
	}
	
	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "configuration.xml";
	
	private OptionContainer optionContainer;
	private ArrayList<LaunchConfig> launchConfigs;
	private transient ArrayList<IChangedListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public Configuration(String path){
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription("The application preferences");
		optionContainer.getOptions().add(new Option(
				OPTIONS.SCHEDULER.toString(), "Run the launch scheduler",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.SCHEDULER_INTERVALL.toString(), "The scheduler intervall in minutes", 
				Type.INTEGER, 5
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.MAXIMUM_AGENTS.toString(), "Maximum number of parallel launches", 
				Type.INTEGER, 3
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.NOTIFY.toString(), "Enable mail notification for application",
				Type.BOOLEAN, false
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.ADMINISTRATORS.toString(), "mail-list of application admins (comma seperated)", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.SMTP_SERVER.toString(), "The SMTP-Server for notifications", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.SMTP_USER.toString(), "The SMTP-User for notifications", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.SMTP_ADDRESS.toString(), "The SMTP-Address for notifications", 
				Type.TEXT, "SMTP@"+Constants.APP_NAME
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.VERBOSE.toString(), "Verbose logging shows more details on console",
				Type.BOOLEAN, false
		));
		
		launchConfigs = new ArrayList<LaunchConfig>();
		listeners = new ArrayList<IChangedListener>();
		this.path = path;
		dirty = true;
	}
	
	/** answers if scheduler is active */
	public boolean isScheduler(){
		return optionContainer.getOption(OPTIONS.SCHEDULER.toString()).getBooleanValue();
	}
	
	/** the scheduler-interval in millis */
	public int getSchedulerIntervall(){ 
		int min = optionContainer.getOption(OPTIONS.SCHEDULER_INTERVALL.toString()).getIntegerValue(); 
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
}
