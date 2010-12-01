package data;

import java.sql.Date;
import java.util.HashMap;

/**
 * option of an item
 */
public class Option {

	public enum Type { TEXT, TEXTAREA, DATE, INTEGER, BOOLEAN }
	
	public enum Properties {
		INTEGER_MIN, INTEGER_MAX
	}

	private String group;
	private String name;
	private String description;
	private Type type;
	private String value;
	private HashMap<String, String> properties;
	
	public Option(String group, String name, String description, Type type, String value){
		
		init(group, name, description, type);
		setStringValue(value);
	}
	
	public Option(String group, String name, String description, Type type, Date value){
		
		init(group, name, description, type);
		setDateValue(value);
	}
	
	public Option(String group, String name, String description, Type type, int value, int min, int max){
		
		init(group, name, description, type);
		setIntegerValue(value);
		properties.put(Properties.INTEGER_MIN.toString(), ""+min);
		properties.put(Properties.INTEGER_MAX.toString(), ""+max);
	}
	
	public Option(String group, String name, String description, Type type, boolean value){
		
		init(group, name, description, type);
		setBooleanValue(value);
	}

	private void init(String group, String name, String description, Type type) {
		
		this.group = group;
		this.name = name;
		this.description = description;
		this.type = type;
		properties = new HashMap<String, String>();
	}
	
	public String getGroup(){ return group; }
	public String getName(){ return name; }
	public String getDescription(){ return description; }
	public Type getType(){ return type; }
	public HashMap<String, String> getProperties(){ return properties; }
	
	public void setStringValue(String value){ this.value = value; }
	public String getStringValue(){ return value; }
	
	public void setDateValue(Date value){ this.value = ""+value.getTime(); }
	public Date getDateValue(){ return new Date(new Long(value).longValue()); }
	
	public void setIntegerValue(int value){ this.value = ""+value; }
	public int getIntegerValue(){ return new Integer(value).intValue(); }
	public int getIntegerMinimum(){
		String property = properties.get(Properties.INTEGER_MIN.toString());
		return property != null ? new Integer(property).intValue() : Integer.MIN_VALUE;
	}
	public int getIntegerMaximum(){
		String property = properties.get(Properties.INTEGER_MAX.toString());
		return property != null ? new Integer(property).intValue() : Integer.MAX_VALUE;
	}
	
	public void setBooleanValue(boolean value){ this.value = ""+value; }
	public boolean getBooleanValue(){ return new Boolean(value).booleanValue(); }
	
	public String toString(){
		return name+" ("+type+") = "+value;
	}
}
