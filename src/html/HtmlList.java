package html;

import java.util.ArrayList;

public class HtmlList {

	private String name;
	private ArrayList<ListEntry> entries;
	
	public HtmlList(String name){
		this.name = name;
		entries = new ArrayList<ListEntry>();
	}
	
	public void add(String name, String content){
		
		ListEntry entry = new ListEntry(name, content);
		entries.add(entry);
	}
	
	public String getHtml(){
		
		StringBuilder html = new StringBuilder();
		if(name!=null){
			html.append("<h3>"+name+"</h3>\n");
		}
		html.append("<ul>\n");
		for(ListEntry entry : entries){
			html.append(entry.getHtml());
		}
		html.append("</ul>\n");
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
			return "<li><b>"+name+"</b>: "+content+"</li>\n";
		}
	}
}
