package core;

import java.io.File;

import launch.PropertyContainer;
import launch.Property;

import logger.Logger;
import logger.ILogConfig.Module;
import util.FileTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * the cache of the application,- will be serialized
 */
public class Cache implements ISystemComponent {

	public static Cache create(Configuration configuration, FileManager fileManager, Logger logger) throws Exception {
		
		File file = new File(fileManager.getDataFolderPath()+File.separator+Cache.OUTPUT_FILE);
		if(file.isFile()){
			return Cache.load(configuration, logger, file.getAbsolutePath());
		}else{
			return new Cache(configuration, logger, file.getAbsolutePath());
		}	
	}
	
	public static final String OUTPUT_FILE = "Cache.xml";
	
	private transient Configuration configuration;
	private transient Logger logger;
	
	@SuppressWarnings("unused")
	private String version;
	private PropertyContainer container;
	private transient String path;
	private transient boolean dirty;
	
	public Cache(Configuration configuration, Logger logger, String path){
		
		this.configuration = configuration;
		this.logger = logger;
		
		version = Constants.APP_VERSION;
		container = new PropertyContainer();
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
	
	public void setProperty(String id, String key, String value){
		
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
	
	public String getProperty(String id, String key){
		
		synchronized(container){
			Property property = container.getProperty(id, key);
			if(property != null){
				return property.value;
			}else{
				return null;
			}
		}
	}
	
	public static Cache load(Configuration configuration, Logger logger, String path) throws Exception {
		
		logger.debug(Module.COMMON, "load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Cache cache = (Cache)xstream.fromXML(xml);
		cache.configuration = configuration;
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
		logger.debug(Module.COMMON, "cleanup cache");
	}
}
