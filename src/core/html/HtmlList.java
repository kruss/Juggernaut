package core.html;

import java.util.ArrayList;

public class HtmlList {

	public enum Type { UL, OL }
	
	private String name;
	private String description;
	private ArrayList<ListEntry> entries;
	private Type type;
	
	public HtmlList(String name){
		this.name = name;
		description = null;
		entries = new ArrayList<ListEntry>();
		type = Type.UL;
	}
	
	public void setDescription(String description){ this.description = description; }
	public void setType(Type type){ this.type = type; }
	public int getSize(){ return entries.size(); }
	
	public void addEntry(String name, String content){
		
		ListEntry entry = new ListEntry(name, content);
		entries.add(entry);
	}
	
	public String getHtml(){
		
		StringBuilder html = new StringBuilder();
		if(name!=null){
			html.append("<h3>"+name+"</h3>\n");
		}
		if(description!=null){
			html.append("<p>"+description+"</p>\n");
		}
		html.append("<"+type.toString()+">\n");
		for(ListEntry entry : entries){
			html.append(entry.getHtml());
		}
		html.append("</"+type.toString()+">\n");
		return html.toString();
	}
	
	class ListEntry {
		public String name;
		public String content;
		
		public ListEntry(String name, String content){
			this.name = name;
			this.content = content;
		}
		
		public String getHtml(){
			if(name != null){
				return "<li><b>"+name+"</b>: "+content+"</li>\n";
			}else{
				return "<li>"+content+"</li>\n";
			}
		}
	}
}
