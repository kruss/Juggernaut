package core.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;



import util.FileTools;
import util.IChangeListener;
import util.IChangeable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.Constants;
import core.ISystemComponent;
import core.launch.data.StatusManager.Status;
import core.launch.history.LaunchHistory;
import core.runtime.FileManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

/**
 * the configuration of the application,- will be serialized
 */
public class History implements ISystemComponent, IChangeable {

	public static History create(Configuration configuration, FileManager fileManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+History.OUTPUT_FILE);
		if(file.isFile()){
			return History.load(configuration, logger, file.getAbsolutePath());
		}else{
			return new History(configuration, logger, file.getAbsolutePath());
		}	
	}
	
	public static final String OUTPUT_FILE = "History.xml";
	
	private transient Configuration configuration;
	private transient Logger logger;
	
	@SuppressWarnings("unused")
	private String version;
	private ArrayList<LaunchHistory> entries;
	private transient ArrayList<IChangeListener> listeners;
	private transient String path;
	private transient boolean dirty;

	public History(Configuration configuration, Logger logger, String path){
		
		this.configuration = configuration;
		this.logger = logger;
		
		version = Constants.APP_VERSION;
		entries = new ArrayList<LaunchHistory>();
		listeners = new ArrayList<IChangeListener>();
		this.path = path;
		dirty = true;
	}
	
	@Override
	public void init() throws Exception {
		save();
	}

	@Override
	public void shutdown() throws Exception {}

	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public ArrayList<LaunchHistory> getEntries(){ return entries; }
	public String getPath(){ return path; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public static History load(Configuration configuration, Logger logger, String path) throws Exception {
	
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		History history = (History)xstream.fromXML(xml);
		history.configuration = configuration;
		history.logger = logger;
		history.listeners = new ArrayList<IChangeListener>();
		history.path = path;
		history.dirty = false;
		return history;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			logger.debug(Module.COMMON, "save: "+path);
			XStream xstream = new XStream(new DomDriver());
			String xml = xstream.toXML(this);
			FileTools.writeFile(path, xml, false);
			dirty = false;
			notifyListeners();
		}
	}
	
	public synchronized void add(LaunchHistory entry) throws Exception {
		
		entries.add(0, entry);
		dirty = true;
		save();
		cleanup();
	}
	
	public synchronized void delete(String id) {
		
		for(int i=0; i<entries.size(); i++){
			LaunchHistory entry = entries.get(i);
			if(entry.historyId.equals(id)){
				delete(entry);
			}
		}
	}
	
	/** clear old entries */
	public synchronized void clear() {
		
		for(int i = entries.size()-1; i>=0; i--){
			delete(entries.get(i));
		}
	}

	/** remove old entries */
	private void cleanup() {
		
		int max = configuration.getMaximumHistory();
		if(max > 0){
			while(max < entries.size()){
				delete(entries.get(entries.size()-1));
			}
		}
	}
	
	private void delete(LaunchHistory entry) {
		
		logger.debug(Module.COMMON, "deleting history: "+entry.id);
		try{
			entries.remove(entry);
			FileTools.deleteFolder(entry.folder);
			dirty = true;
			save();
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
	}
	
	/** get the latest entry of specified id */
	public synchronized LaunchHistory getLatest(String id) {
		
		for(LaunchHistory entry : entries){
			if(entry.id.equals(id)){
				return entry;
			}
		}
		return null;
	}
	
	/** get the previous entry of specified one */
	public synchronized LaunchHistory getPrevious(LaunchHistory entry) {

		int index = entries.indexOf(entry);
		if(index != -1){
			for(int i = index+1; i<entries.size(); i++){
				if(entries.get(i).id.equals(entry.id)){
					return entries.get(i);
				}
			}
		}
		return null;
	}
	
	public class HistoryInfo {
		
		public String historyId;
		public String name;
		public String id;
		public String logfile;
		public String trigger;
		public Date start;
		public Date end;
		public Status status;
		public String folder;
		
		public HistoryInfo(LaunchHistory entry){
			historyId = entry.historyId;
			name = entry.name;
			id = entry.id;
			logfile = entry.logfile;
			trigger = entry.trigger;
			start = entry.start;
			end = entry.end;
			status = entry.status;
			folder = entry.folder;
		}
	}
	
	public synchronized ArrayList<HistoryInfo> getHistoryInfo(){
		
		ArrayList<HistoryInfo> infos = new ArrayList<HistoryInfo>();
		for(LaunchHistory entry : entries){
			infos.add(new HistoryInfo(entry));
		}
		return infos;
	}
	
	public synchronized ArrayList<HistoryInfo> getHistoryInfo(String name){
		
		ArrayList<HistoryInfo> infos = new ArrayList<HistoryInfo>();
		for(LaunchHistory entry : entries){
			if(entry.name.equals(name)){
				infos.add(new HistoryInfo(entry));
			}
		}
		return infos;
	}
	
	public synchronized ArrayList<String> getHistoryNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(LaunchHistory entry : entries){
			if(!names.contains(entry.name)){
				names.add(entry.name);
			}
		}
		Collections.sort(names);
		return names;
	}
}
