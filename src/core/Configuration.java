package core;

import java.util.ArrayList;

import util.FileTools;
import util.IChangedListener;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import data.LaunchConfig;


/**
 * the configuration of the application,- will be serialized
 */
public class Configuration {

	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "configuration.xml";
	
	private ArrayList<LaunchConfig> configs;
	private transient ArrayList<IChangedListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public Configuration(String path){
		
		configs = new ArrayList<LaunchConfig>();
		
		listeners = new ArrayList<IChangedListener>();
		this.path = path;
		dirty = true;
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public ArrayList<LaunchConfig> getLaunchConfigs(){ return configs; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	
	public boolean isDirty(){ 
		if(dirty){ return true; }
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
		configuration.listeners = new ArrayList<IChangedListener>();
		configuration.path = path;
		for(LaunchConfig config : configuration.configs){
			config.setDirty(false);
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
			for(LaunchConfig config : configs){
				config.setDirty(false);
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
