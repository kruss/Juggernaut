package core;

import java.util.ArrayList;

import util.FileTools;
import util.IChangedListener;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import data.LaunchHistory;

/**
 * the configuration of the application,- will be serialized
 */
public class History {

	public static final String OUTPUT_FILE = "History.xml";
	
	private ArrayList<LaunchHistory> entries;
	private transient ArrayList<IChangedListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public History(String path){
		
		entries = new ArrayList<LaunchHistory>();
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
	
	public ArrayList<LaunchHistory> getEntries(){ return entries; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public static History load(String path) throws Exception {
	
		Application.getInstance().getLogger().debug("load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		History history = (History)xstream.fromXML(xml);
		history.listeners = new ArrayList<IChangedListener>();
		history.path = path;
		history.dirty = false;
		return history;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			Application.getInstance().getLogger().debug("save: "+path);
			XStream xstream = new XStream(new DomDriver());
			String xml = xstream.toXML(this);
			FileTools.writeFile(path, xml, false);
			dirty = false;
			notifyListeners();
		}
	}
	
	public synchronized void addEntry(LaunchHistory entry) throws Exception {
		
		entry.init();
		entries.add(0, entry);
		cleanup();
		dirty = true;
		save();
	}

	private void cleanup() {
		
		// TODO delete history entries if history-max was reached
	}
}
