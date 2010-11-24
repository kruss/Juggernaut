package data;

import java.util.ArrayList;



/**
 * container for all options of an item
 */
public class OptionContainer {
	
	private String description;
	private ArrayList<Option> options;

	public OptionContainer(){
		
		description = "";
		options = new ArrayList<Option>();
	}
	
	public void setDescription(String description){ this.description = description; }
	public String getDescription(){ return description; }
	
	public void setOptions(ArrayList<Option> options){ this.options = options; }
	public ArrayList<Option> getOptions(){ return options; }
	
	public ArrayList<String> getOptionNames(){
		
		ArrayList<String> names = new ArrayList<String>();
		for(Option option : options){
			names.add(option.getName());
		}
		return names;
	}
	
	public Option getOption(String name){
	
		for(Option option : options){
			if(option.getName().equals(name)){
				return option;
			}
		}
		return null;
	}
	
	public String toString(){
		
		StringBuilder info = new StringBuilder();
		for(Option option : options){
			info.append(option.getName()+": "+option.getStringValue()+"\n");
		}
		return info.toString();
	}
}
