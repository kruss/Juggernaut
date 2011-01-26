package core.launch.data;


import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import core.html.HtmlLink;
import core.launch.data.StatusManager.Status;

import util.FileTools;


public class Artifact {
	
	public String name;
	public String description;
	private String content;
	private String type;
	private File file;
	public Status status;
	
	public ArrayList<Artifact> childs;
	
	public Artifact(String name){
		
		init(name);
	}
	
	public Artifact(String name, String content, String type){
		
		init(name);
		this.content = content;
		this.type = type;
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
				UUID.randomUUID().toString()+"."+type;
			FileTools.writeFile(path, content, false);
			file = new File(path);
			content = null;
		}
		
		for(Artifact child : childs){
			child.finish(folder);
		}
	}
	
	public String toHtml(){
		
		StringBuilder html = new StringBuilder();
		if(file != null){
			HtmlLink link = new HtmlLink(name, file.getName());
			if(file.isDirectory()){
				link.setExtern(true);
			}
			html.append("<b>"+link.getHtml()+"</b>");
		}else{
			html.append("<b>"+name+"</b>");
		}
		if(description != null){
			html.append(" - "+description);
		}
		if(status != null){
			html.append(" - "+StatusManager.getStatusHtml(status));
		}
		return html.toString();
	}
}
