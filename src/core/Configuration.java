package core;

import java.util.ArrayList;

import util.FileTools;
import util.IChangeListener;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import launch.LaunchConfig;

/**
 * the configuration of the application,- will be serialized
 */
public class Configuration {

	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "config.xml";
	
	private ArrayList<LaunchConfig> configs;
	private transient ArrayList<IChangeListener> listeners;
	private transient String path;

	public Configuration(String path){
		
		configs = new ArrayList<LaunchConfig>();
		
		listeners = new ArrayList<IChangeListener>();
		this.path = path;
	}
	
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public ArrayList<LaunchConfig> getLaunchConfigs(){ return configs; }
	public String getPath(){ return path; }
	
	public boolean isDirty(){ 
		for(LaunchConfig config : configs){
			if(config.isDirty()){ return true; }
		}
		return false;
	}
	
	public State getState(){ return isDirty() ? State.DIRTY : State.CLEAN; }
	
	public static Configuration load(String path) throws Exception {
	
		Application.getInstance().getLogger().debug("load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.listeners = new ArrayList<IChangeListener>();
		configuration.path = path;
		for(LaunchConfig config : configuration.configs){
			config.setDirty(false);
		}
		return configuration;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			Application.getInstance().getLogger().debug("save: "+path);
			XStream xstream = new XStream(new DomDriver());
			String xml = xstream.toXML(this);
			FileTools.writeFile(path, xml, false);
			for(LaunchConfig config : configs){
				config.setDirty(false);
			}
			notifyListeners();
		}
	}

	public void chekForSave() throws Exception {
		
		if(isDirty() && UiTools.confirmDialog("Save changes ?")){
			save();
		}
	}
}
