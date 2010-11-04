package util;

import java.sql.Date;

/**
 * attribute of an item
 */
public class Attribute {

	public enum Type { TEXT, TEXTFIELD, DATE, INTEGER, BOOLEAN }

	private String name;
	private String description;
	private Type type;
	private String value;
	
	public Attribute(String name, String description, Type type, String value){
		
		this.name = name;
		this.description = description;
		this.type = type;
		setStringValue(value);
	}
	
	public Attribute(String name, String description, Type type, Date value){
		
		this.name = name;
		this.description = description;
		this.type = type;
		setDateValue(value);
	}
	
	public Attribute(String name, String description, Type type, int value){
		
		this.name = name;
		this.description = description;
		this.type = type;
		setIntegerValue(value);
	}
	
	public Attribute(String name, String description, Type type, boolean value){
		
		this.name = name;
		this.description = description;
		this.type = type;
		setBooleanValue(value);
	}
	
	public String getName(){ return name; }
	public String getDescription(){ return description; }
	public Type getType(){ return type; }
	
	public void setStringValue(String value){ this.value = value; }
	public String getStringValue(){ return value; }
	
	public void setDateValue(Date value){ this.value = ""+value.getTime(); }
	public Date getDateValue(){ return new Date(new Long(value).longValue()); }
	
	public void setIntegerValue(int value){ this.value = ""+value; }
	public int getIntegerValue(){ return new Integer(value).intValue(); }
	
	public void setBooleanValue(boolean value){ this.value = ""+value; }
	public boolean getBooleanValue(){ return new Boolean(value).booleanValue(); }
	
	public String toString(){
		return name+" ("+type+") = "+value;
	}
}
