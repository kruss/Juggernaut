package launch;

public class Property {
	
	public String id; 
	public String key; 
	public String value;
	
	public Property(String id, String key, String value){
		this.id = id;
		this.key = key;
		this.value = value;
	}

	public String toString(){
		return id+"@"+key+"="+value;
	}
}
