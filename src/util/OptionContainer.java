package util;

import java.util.ArrayList;


/**
 * container for all options of an item
 */
public class OptionContainer {
	
	private ArrayList<Option> options;

	public OptionContainer(){
		
		options = new ArrayList<Option>();
	}
	
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
}
