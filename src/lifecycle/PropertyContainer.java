package lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PropertyContainer {

	private static final String DELIM = "::";
	
	private HashMap<String, String> properties;
	
	public PropertyContainer() {
		
		properties = new HashMap<String, String>();
	}

	public void addProperties(String id, HashMap<String, String> map){
		
		ArrayList<String> names = getKeys(map);
		for(String name : names){
			addProperty(id, name, map.get(name));
		}
	}
	
	public void addProperty(String id, String name, String value){
		properties.put(getKey(id, name), value);
	}
	
	public String getProperty(String id, String name){
		return properties.get(getKey(id, name));
	}
	
	public HashMap<String, String> getProperties(String id){
		
		HashMap<String, String> map = new HashMap<String, String>();
		ArrayList<String> keys = getKeys(properties);
		for(String key : keys){
			if(key.startsWith(id)){
				map.put(getName(key), properties.get(key));
			}
		}
		return map;
	}

	private String getKey(String id, String name) {
		return id+DELIM+name;
	}
	
	private String getName(String key){
		return key.substring(key.indexOf(DELIM)+DELIM.length(), key.length());
	}
	
	public static ArrayList<String> getKeys(HashMap<String, String> map) {
		String[] keys = map.keySet().toArray(new String[1]);
		ArrayList<String> list = new ArrayList<String>();
		for(String key : keys){
			list.add(key);
		}
		Collections.sort(list);
		return list;
	}
	
	public static String expand(PropertyContainer container, String value) {
		
		while(true){
			int a = value.indexOf("{", 0);
			int b = value.indexOf("}", a);
			if(a!=-1 && b!=-1){
				String key = value.substring(a+1, b);
				String expand = container.properties.get(key);
				if(expand == null){
					expand = "?"+key+"?";
				}
				value = value.substring(0, a)+expand+value.substring(b+1, value.length());
			}else{
				break;
			}
		}
		return value;
	}
}
