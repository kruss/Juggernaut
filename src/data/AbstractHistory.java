package data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import util.FileTools;
import data.Artifact.Attachment;

import lifecycle.StatusManager.Status;

public abstract class AbstractHistory {

	public String historyId;
	
	public String id;
	public String name;
	public String description;
	public Date start;
	public Date end;
	public Status status;
	public String folder;
	
	public ArrayList<Artifact> artifacts;
	
	public AbstractHistory(){
		
		historyId = UUID.randomUUID().toString();
		artifacts = new ArrayList<Artifact>();
	}
	
	public void init() throws Exception {
		
		FileTools.createFolder(folder);
		for(Artifact artifact : artifacts){
			for(Attachment attachment : artifact.attachments){
				File source = new File(attachment.path);
				File destination = new File(folder+File.separator+source.getName());
				if(source.isFile()){
					FileTools.copyFile(source.getAbsolutePath(), destination.getAbsolutePath());
				}else if(source.isDirectory()){
					FileTools.copyFolder(source.getAbsolutePath(), destination.getAbsolutePath());
				}
			}
		}
	}
}
