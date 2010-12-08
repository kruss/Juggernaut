package core;

import launch.PropertyContainer;
import util.FileTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * the cache of the application,- will be serialized
 */
public class Cache {

	public static final String OUTPUT_FILE = "Cache.xml";
	
	@SuppressWarnings("unused")
	private String version;
	
	private PropertyContainer propertyContainer;
	
	private transient String path;
	private transient boolean dirty;
	
	public Cache(String path){
		
		version = Constants.APP_VERSION;
		propertyContainer = new PropertyContainer();
		this.path = path;
		dirty = true;
	}
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }
	
	public synchronized void addProperty(String id, String name, String value){
		
		propertyContainer.addProperty(id, name, value);
		dirty = true;
		try{ 
			save(); 
		}catch(Exception e){
			Application.getInstance().getLogger().error(e);
		}
	}
	
	public synchronized String getProperty(String id, String name){
		return propertyContainer.getProperty(id, name);
	}
	
	public static Cache load(String path) throws Exception {
		
		Application.getInstance().getLogger().debug("load: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Cache cache = (Cache)xstream.fromXML(xml);
		cache.path = path;
		cache.dirty = false;
		return cache;
	}
	
	public void save() throws Exception {
		
		if(isDirty()){
			Application.getInstance().getLogger().debug("save: "+path);
			XStream xstream = new XStream(new DomDriver());
			String xml = xstream.toXML(this);
			FileTools.writeFile(path, xml, false);
			dirty = false;
		}
	}
}
