package data;

import html.AbstractHtmlPage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import util.FileTools;

import launch.LifecycleObject;
import launch.StatusManager.Status;

public abstract class AbstractHistory {

	private transient LifecycleObject object;
	
	public String historyId;
	
	public String id;
	public String name;
	public String description;
	public Date start;
	public Date end;
	public Status status;
	public String folder;
	public ArrayList<Artifact> artifacts;
	
	public AbstractHistory(LifecycleObject object){
		
		this.object = object;
		historyId = UUID.randomUUID().toString();
		artifacts = new ArrayList<Artifact>();
	}
	
	public void init() throws Exception {
		
		FileTools.createFolder(folder);
	}
	
	public void finish() throws Exception {
		
		artifacts.addAll(object.getArtifacts());
		for(Artifact artifact : artifacts){
			artifact.finish(new File(folder));
		}
		createHtml();
	}

	private void createHtml() throws Exception {

		AbstractHtmlPage page = getHtmlPage();
		page.create();
	}

	protected abstract AbstractHtmlPage getHtmlPage();
}
