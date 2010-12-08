package data;

import java.util.ArrayList;

import launch.StatusManager.Status;

public class Artifact implements Comparable<Artifact> {

	public static final String DEFAULT_TYPE = "DEFAULT";

	public enum Action { KEEP, COPY, MOVE }
	
	public String type;
	public String name;
	public String description;
	public Status status;
	
	public ArrayList<Attachment> attachments;
	
	public Artifact(String name){
		
		this.name = name;
		type = DEFAULT_TYPE;
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
