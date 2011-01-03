package core;

import java.io.File;

import launch.PropertyContainer;

import logger.Logger;
import logger.ILogConfig.Module;
import util.FileTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * the cache of the application,- will be serialized
 */
public class Cache implements ISystemComponent {

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
	
	@SuppressWarnings("unused")
	private String version;
	private PropertyContainer propertyContainer;
	private transient String path;
	private transient boolean dirty;
	
	public Cache(Logger logger, String path){
		
		this.logger = logger;
		
		version = Constants.APP_VERSION;
		propertyContainer = new PropertyContainer();
		this.path = path;
		dirty = true;
	}
	
	@Override
	public void init() throws Exception {
		save();
	}

	@Override
	public void shutdown() throws Exception {
		cleanup();
	}
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public synchronized void addProperty(String id, String name, String value){
		
		propertyContainer.addProperty(id, name, value);
		dirty = true;
		try{ 
			save(); 
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
	}
	
	public synchronized String getProperty(String id, String name){
		return propertyContainer.getProperty(id, name);
	}
	
	public static Cache load(Logger logger, String path) throws Exception {
		
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Cache cache = (Cache)xstream.fromXML(xml);
		cache.logger = logger;
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
		}
	}

	public void cleanup() {
		// TODO clean cache
	}
}
