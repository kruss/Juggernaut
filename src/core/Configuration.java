package core;

import java.util.ArrayList;

import util.FileTools;
import util.UiTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import launch.ILaunchConfig;

/**
 * the configuration storage
 */
public class Configuration {

	public static final String OUTPUT_FILE = "configuration.xml";
	
	private transient String path;
	
	private ArrayList<ILaunchConfig> configurations;

	public Configuration(String path){
		
		this.path = path;
		configurations = new ArrayList<ILaunchConfig>();
	}
	
	public ArrayList<ILaunchConfig> getConfigurations(){ return configurations; }
	
	boolean isDirty(){
		
		for(ILaunchConfig configuration: configurations){
			if(configuration.isDirty()){ return true; }
		}
		return false;
	}
	
	public static Configuration load(String path) throws Exception {
	
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Configuration configuration = (Configuration)xstream.fromXML(xml);
		configuration.path = path;
		return configuration;
	}
	
	public void save() throws Exception {
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		FileTools.writeFile(path, xml, false);
	}

	public void chekForSave() throws Exception {
		
		if(isDirty() && UiTools.confirmDialog("Save changes ?")){
			save();
		}
	}
}
