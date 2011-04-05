package core.persistence;

import java.io.File;
import java.util.ArrayList;


import util.FileTools;
import util.IChangeListener;
import util.IChangeable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.Constants;
import core.ISystemComponent;
import core.launch.LaunchConfig;
import core.launch.data.property.Property;
import core.launch.data.property.PropertyContainer;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.runtime.FileManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;


/**
 * the cache of the application,- will be serialized
 */
public class Cache implements ISystemComponent, IChangeable {

	public static Cache create(FileManager fileManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+Cache.OUTPUT_FILE);
		if(file.isFile()){
			return Cache.load(logger, file.getAbsolutePath());
		}else{
			return new Cache(logger, file.getAbsolutePath());
		}	
	}
	
	public static final String OUTPUT_FILE = "Cache.xml";
	
	private transient Logger logger;
	private transient ArrayList<IChangeListener> listeners;
	
	@SuppressWarnings("unused")
	private String version;
	private PropertyContainer container;
	private transient String path;
	private transient boolean dirty;
	
	public Cache(Logger logger, String path){
		
		this.logger = logger;
		version = Constants.APP_VERSION;
		container = new PropertyContainer();
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
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public void setValue(String id, String key, String value){
		
		synchronized(container){
			container.setProperty(new Property(id, key, value));
			dirty = true;
			try{ 
				save(); 
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
	
	public String getValue(String id, String key){
		
		synchronized(container){
			Property property = container.getProperty(id, key);
			if(property != null){
				return property.value;
			}else{
				return null;
			}
		}
	}
	
	public void removeValue(String id, String key){
		
		synchronized(container){
			container.removeProperty(id, key);
			dirty = true;
			try{ 
				save(); 
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
	
	public void removeValues(String id){
		
		synchronized(container){
			container.removeProperties(id);
			dirty = true;
			try{ 
				save(); 
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
	
	public void clear(){
		
		synchronized(container){
			container.clear();
			dirty = true;
			try{ 
				save(); 
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
	
	public static Cache load(Logger logger, String path) throws Exception {
		
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Cache cache = (Cache)xstream.fromXML(xml);
		cache.logger = logger;
		cache.listeners = new ArrayList<IChangeListener>();
		cache.path = path;
		cache.dirty = false;
		return cache;
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

	/** get info on entries related to configuration */
	public ArrayList<CacheInfo> getInfo(Configuration configuration){
		
		ArrayList<CacheInfo> info = new ArrayList<CacheInfo>();
		// info on configuration related items
		for(LaunchConfig launchConfig : configuration.getLaunchConfigs()){
			String launchIdentifier = "Launch("+launchConfig.getName()+")";
			for(Property property : container.getProperties(launchConfig.getId())){
				info.add(new CacheInfo(launchIdentifier, property));
			}
			for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
				String operationIdentifier = launchIdentifier+"::Operation("+operationConfig.getUIName()+")";
				for(Property property : container.getProperties(operationConfig.getId())){
					info.add(new CacheInfo(operationIdentifier, property));
				}
			}
			for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
				String triggerIdentifier = launchIdentifier+"::Trigger("+triggerConfig.getName()+")";
				for(Property property : container.getProperties(triggerConfig.getId())){
					info.add(new CacheInfo(triggerIdentifier, property));
				}
			}
		}
		// info on non-configuration related items
		for(String id : getNonConfigurationIds(configuration)){
			for(Property property : container.getProperties(id)){
				info.add(new CacheInfo("<"+id+">", property));
			}
		}
		return info;
	}

	public class CacheInfo {
		
		public String identifier;
		public String id;
		public String key;
		public String value;
		
		public CacheInfo(String identifier, Property property){
			this.identifier = identifier;
			id = property.id;
			key = property.key;
			value = property.value;
		}
	}
	
	/** delete all entries not related to configuration */
	public void cleanup(Configuration configuration) throws Exception {
		
		synchronized(container){
			ArrayList<String> ids = getNonConfigurationIds(configuration);
			if(ids.size() > 0){
				for(String id : ids){
					container.removeProperties(id);
				}
				dirty = true;
				save();
			}
		}
	}

	/** get all ids wich are not related to configuration */
	private ArrayList<String> getNonConfigurationIds(Configuration configuration) {

		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> cacheIds = container.getIds();
		ArrayList<String> configurationIds = getConfigurationIds(configuration);
		for(String id : cacheIds){
			if(!configurationIds.contains(id)){
				ids.add(id);
			}
		}
		return ids;
	}

	/** get all ids wich are related to configuration */
	private ArrayList<String> getConfigurationIds(Configuration configuration) {
		
		ArrayList<String> ids = new ArrayList<String>();
		for(LaunchConfig config : configuration.getLaunchConfigs()){
			ids.addAll(getLaunchConfigurationIds(config));
		}
		return ids;
	}
	
	/** get all ids wich are related to a launch */
	private ArrayList<String> getLaunchConfigurationIds(LaunchConfig config) {
		
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(config.getId());
		for(AbstractOperationConfig operation : config.getOperationConfigs()){
			ids.add(operation.getId());
		}
		for(AbstractTriggerConfig trigger : config.getTriggerConfigs()){
			ids.add(trigger.getId());
		}
		return ids;
	}
}
