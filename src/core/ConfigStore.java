package core;

import java.util.ArrayList;

import util.FileTools;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import launch.LaunchConfig;

/**
 * the configuration of all launches
 */
public class ConfigStore {

	public enum State { CLEAN, DIRTY }
	public static final String OUTPUT_FILE = "config.xml";
	
	private ArrayList<LaunchConfig> configs;
	private transient ArrayList<IChangeListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public ConfigStore(String path){
		
		configs = new ArrayList<LaunchConfig>();
		
		listeners = new ArrayList<IChangeListener>();
		this.path = path;
		dirty = true;
	}
	
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public ArrayList<LaunchConfig> getLaunchConfigs(){ return configs; }
	public String getPath(){ return path; }
	public boolean isDirty(){ return dirty; }
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public State getState(){ return dirty ? State.DIRTY : State.CLEAN; }
	
	public static ConfigStore load(String path) throws Exception {
	
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		ConfigStore store = (ConfigStore)xstream.fromXML(xml);
		store.listeners = new ArrayList<IChangeListener>();
		store.path = path;
		store.dirty = false;
		return store;
	}
	
	public void save() throws Exception {
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		FileTools.writeFile(path, xml, false);
		dirty = false;
		notifyListeners();
	}

	public void chekForSave() throws Exception {
		
		if(isDirty() && UiTools.confirmDialog("Save changes ?")){
			save();
		}
	}
}
