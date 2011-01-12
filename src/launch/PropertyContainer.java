package launch;

import java.util.ArrayList;

public class PropertyContainer {
	
	private ArrayList<Property> entries;
	
	public PropertyContainer() {
		entries = new ArrayList<Property>();
	}
	
	public void setProperty(Property property){
		
		Property entry = getProperty(property.id, property.key);
		if(entry != null){
			entry.value = property.value;
		}else{
			entries.add(property);
		}
	}
	
	public Property getProperty(String id, String key){
		
		for(Property entry : entries){
			if(entry.id.equals(id) && entry.key.equals(key)){
				return entry;
			}
		}
		return null;
	}
	
	public void setProperties(ArrayList<Property> properties){
		
		for(Property property : properties){
			setProperty(property);
		}
	}
	
	public ArrayList<Property> getProperties(String id){
		
		ArrayList<Property> list = new ArrayList<Property>();
		for(Property entry : entries){
			if(entry.id.equals(id)){
				list.add(entry);
			}
		}
		return list;
	}
	
	/** expand all properties within value of syntax {id@key} */
	public String expand(String string) {
		
		int index = 0;
		while(true){
			int left = string.indexOf("{", index);
			int right = string.indexOf("}", left);
			if(left != -1 && right != -1){
				index = left+1;
				int delim = string.indexOf("@", left);
				if(delim != -1 && delim < right){
					String id = string.substring(left+1, delim);
					String name = string.substring(delim+1, right); 
					
					Property property = getProperty(id, name);
					if(property != null){
						string = string.substring(0, left)+property.value+string.substring(right+1, string.length());
					}
				}
			}else{
				break;
			}
		}
		return string;
	}
}
