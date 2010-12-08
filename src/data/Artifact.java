package data;

import java.util.ArrayList;

import launch.StatusManager.Status;

public class Artifact {

	public String name;
	public String description;
	public String content;
	public Status status;
	
	public ArrayList<Artifact> childs;
	
	public Artifact(String name){
		
		this.name = name;
		description = "";
		content = "";
		status = Status.UNDEFINED;
		childs = new ArrayList<Artifact>();
	}
}
