package ui.option;

import java.util.ArrayList;

import core.launch.data.property.PropertyContainer;

import ui.option.Option.Type;




/**
 * container for options of an item
 */
public class OptionContainer {
	
	private ArrayList<Option> options;
	private String description;

	public OptionContainer(){
		
		options = new ArrayList<Option>();
		description = "";
	}
	
	public void setDescription(String description){ this.description = description; }
	public String getDescription(){ return description; }
	
	public ArrayList<Option> getOptions(){ return options; }
	
	public void setOption(Option option){
		
		if(!hasOption(option.getName())){
			options.add(option);
		}
	}
	
	private boolean hasOption(String name) {
		return getOption(name) != null;
	}

	public Option getOption(String name){
	
		for(Option option : options){
			if(option.getName().equals(name)){
				return option;
			}
		}
		return null;
	}
	
	public void expand(PropertyContainer container){
		
		for(Option option : options){
			String value = option.getStringValue();
			String expand = container.expand(value);
			option.setStringValue(expand);
		}
	}

	public String toString() {
		
		StringBuilder text = new StringBuilder();
		for(Option option : options){
			if(!option.getStringValue().isEmpty()){	
				String name = option.getGroup()+"::"+option.getName();
				String value = 
					(option.getType() == Type.TEXT_AREA) ?
					option.getStringValue().replaceAll("\\n", "\\\\n") : 
					option.getStringValue();
				text.append(name+"=["+value+"]\n");
			}
		}
		return text.toString();
	}
	
	public String toHtml() {
		
		StringBuilder html = new StringBuilder();
		html.append("<ul>");
		for(Option option : options){
			if(!option.getStringValue().isEmpty()){	
				String name = option.getGroup()+"::"+option.getName();
				String value = 
						(option.getType() == Type.TEXT_AREA) ?
						"<br>"+option.getStringValue().replaceAll("\\n", "<br>") : 
						option.getStringValue();
				html.append("<li>"+name+": <b>"+value+"</b></li>");
			}
		}
		html.append("</ul>");
		return html.toString();
	}
}
