package data;

import html.AbstractHtmlPage;

import java.util.Date;
import java.util.UUID;

import util.FileTools;

import launch.StatusManager.Status;

public abstract class AbstractHistory {

	public String historyId;
	
	public String id;
	public String name;
	public String description;
	public Date start;
	public Date end;
	public Status status;
	public String folder;
	
	public AbstractHistory(){
		
		historyId = UUID.randomUUID().toString();
	}
	
	public void init() throws Exception {
		
		FileTools.createFolder(folder);
	}
	
	public void finish() throws Exception {
		
		createHtml();
	}

	private void createHtml() throws Exception {

		AbstractHtmlPage page = getHtmlPage();
		page.create();
	}

	protected abstract AbstractHtmlPage getHtmlPage();
}
