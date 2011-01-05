package data;

public class Error {
	
	public String id;
	public String message;
	
	public Error(String id, String message){
		this.id = id;
		this.message = message;	
	}
	
	public String getHtml() {
		return "<font color='red'>"+message+"</font>";	
	}
	
	public long getHash(){
		return (id+message).hashCode();	
	}
}
