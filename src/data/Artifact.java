package data;

import html.HtmlLink;

import java.io.File;
import java.util.ArrayList;

import launch.StatusManager;
import launch.StatusManager.Status;

public class Artifact {
	
	public String name;
	public String description;
	public File file;
	public String content;
	public Status status;
	
	public ArrayList<Artifact> childs;
	
	public Artifact(String name, File file){
		
		init(name);
		this.file = file;
	}
	
	public Artifact(String name, String content){
		
		init(name);
		this.content = content;
	}
	
	public void init(String name){
		
		this.name = name;
		description = null;
		file = null;
		content = null;
		status = null;
		childs = new ArrayList<Artifact>();
	}
	
	public String toHtml(){
		
		StringBuilder html = new StringBuilder();
		if(file != null){
			HtmlLink link = new HtmlLink(name, file.getName());
			html.append("<b>"+link.getHtml()+"</b>");
		}else{
			html.append("<b>"+name+"</b>");
		}
		if(status != null){
			html.append(" ("+StatusManager.getStatusHtml(status)+")");
		}
		if(description != null){
			html.append(" - <i>"+description+"</i>");
		}
		if(content != null){
			html.append("<p>"+content.replaceAll("\\n", "<br>\n")+"<p>");
		}
		return html.toString();
	}
}
