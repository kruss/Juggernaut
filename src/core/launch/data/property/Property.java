package core.launch.data.property;

public class Property implements Comparable<Property> {
	
	public String id; 
	public String key; 
	public String value;
	
	public Property(String id, String key, String value){
		this.id = id;
		this.key = key;
		this.value = value;
	}

	public String toString(){
		return getIdentifier()+"="+value;
	}
	
	private String getIdentifier(){
		return id+"@"+key;
	}

	@Override
	public int compareTo(Property o) {
		return getIdentifier().compareTo(o.getIdentifier());
	}
}
