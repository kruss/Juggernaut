package core;

import java.util.ArrayList;

import util.FileTools;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.IConfigStoreListener.State;

import launch.LaunchConfig;

/**
 * the configuration of all launches
 */
public class ConfigStore {

	public static final String OUTPUT_FILE = "config.xml";
	
	private ArrayList<LaunchConfig> configs;
	
	private transient String path;
	private transient boolean dirty;
	private transient ArrayList<IConfigStoreListener> listeners;

	public ConfigStore(String path){
		
		configs = new ArrayList<LaunchConfig>();
		
		this.path = path;
		dirty = true;
		listeners = new ArrayList<IConfigStoreListener>();
	}
	
	public ArrayList<LaunchConfig> getConfigs(){ return configs; }
	public void addListener(IConfigStoreListener listener){ listeners.add(listener); }
	public String getPath(){ return path; }
	public boolean isDirty(){ return dirty; }
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	
	public static ConfigStore load(String path) throws Exception {
	
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		ConfigStore store = (ConfigStore)xstream.fromXML(xml);
		store.listeners = new ArrayList<IConfigStoreListener>();
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
	
	public void notifyListeners(){
		
		State state = dirty ? State.DIRTY : State.CLEAN;
		for(IConfigStoreListener listener : listeners){
			listener.configChanged(state);
		}
	}
}
