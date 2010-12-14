package data;

import html.HtmlLink;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import util.FileTools;

import launch.StatusManager;
import launch.StatusManager.Status;

public class Artifact {
	
	public String name;
	public String description;
	private String content;
	private File file;
	private Status status;
	
	public ArrayList<Artifact> childs;
	
	public Artifact(String name, String content){
		
		init(name);
		this.content = content;
	}
	
	public Artifact(String name, File file){
		
		init(name);
		this.file = file;
	}
	
	public void init(String name){
		
		this.name = name;
		description = null;
		file = null;
		content = null;
		status = null;
		childs = new ArrayList<Artifact>();
	}
	
	public void finish(File folder) throws Exception {
		
		if(content != null){
			String path = 
				folder.getAbsolutePath()+File.separator+
				UUID.randomUUID().toString()+".txt";
			FileTools.writeFile(path, content, false);
			file = new File(path);
			content = null;
		}
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
			html.append(" - "+description);
		}
		return html.toString();
	}
}
