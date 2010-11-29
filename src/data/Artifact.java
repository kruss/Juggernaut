package data;

import java.util.ArrayList;

import lifecycle.StatusManager.Status;

public class Artifact implements Comparable<Artifact> {

	public enum Type { GENERATED, RESULT }
	public enum Action { LEAVE, COPY, MOVE }
	
	public String type;
	public String name;
	public String description;
	public Status status;
	
	public ArrayList<Attachment> attachments;
	
	public Artifact(String type, String name){
		
		this.type = type;
		this.name = name;
		description = "";
		status = Status.UNDEFINED;
		attachments = new ArrayList<Attachment>();
	}
	
	public class Attachment {
		
		public String name;
		public String description;
		public String path;
		public Action action;
		
		public Attachment(String name, String path, Action action){
			
			this.name = name;
			description = "";
			this.path = path;
			this.action = action;
		}
	}

	@Override
	public int compareTo(Artifact o) {
		return (type+"::"+name).compareTo((o.type+"::"+o.name));
	}
}
