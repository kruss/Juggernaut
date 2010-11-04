package model;

import java.util.ArrayList;

/**
 * container for all attributes of an item
 */
public class AttributeContainer {
	
	private ArrayList<Attribute> attributes;

	public AttributeContainer(){
		
		attributes = new ArrayList<Attribute>();
	}
	
	public void setAttributes(ArrayList<Attribute> attributes){ this.attributes = attributes; }
	public ArrayList<Attribute> getAttributes(){ return attributes; }
	
	public ArrayList<String> getAttributeNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(Attribute attribute : attributes){
			names.add(attribute.getName());
		}
		return names;
	}
	
	public Attribute getAttribute(String name) throws Exception {
	
		for(Attribute attribute : attributes){
			if(attribute.getName().equals(name)){
				return attribute;
			}
		}
		throw new Exception("attribute not found: "+name);
	}
}
