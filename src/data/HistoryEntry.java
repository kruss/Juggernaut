package data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import util.FileTools;

import core.Application;
import data.Artifact.Attachment;

import lifecycle.LaunchAgent;
import lifecycle.StatusManager.Status;

public class HistoryEntry {

	public String type;
	public String id;
	public String name;
	public String description;
	public Date start;
	public Date end;
	public Status status;
	public String folder;
	
	public ArrayList<Artifact> artifacts;
	public ArrayList<HistoryEntry> entries;
	
	public HistoryEntry(LaunchAgent launch){

		type = "Launch";
		id = launch.getConfig().getId();
		name = launch.getConfig().getName();
		description = "Trigger: "+launch.getTriggerStatus().message;
		start = launch.getStatusManager().getStart();
		end = launch.getStatusManager().getEnd();
		status = launch.getStatusManager().getStatus();
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+launch.getStatusManager().getStart().getTime();		
		artifacts = launch.getArtifacts();
		entries = new ArrayList<HistoryEntry>();
		for(AbstractOperation operation : launch.getOperations()){
			entries.add(new HistoryEntry(operation));
		}
	}
	
	public HistoryEntry(AbstractOperation operation){
		
		type = "Operation";
		id = operation.getConfig().getId();
		name = operation.getConfig().getName();
		description = operation.getIndex()+". Operation of the Launch";
		start = operation.getStatusManager().getStart();
		end = operation.getStatusManager().getEnd();
		status = operation.getStatusManager().getStatus();
		folder = 
			Application.getInstance().getHistoryFolder()+
			File.separator+operation.getParent().getStatusManager().getStart().getTime()+
			File.separator+id;
		artifacts = operation.getArtifacts();
		entries = new ArrayList<HistoryEntry>();
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
		
		for(HistoryEntry entry : entries){
			entry.init();
		}
	}
}
