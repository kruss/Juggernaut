package html;

import java.util.ArrayList;

import launch.StatusManager;
import data.Error;
import util.DateTools;
import data.AbstractHistory;
import data.Artifact;

public abstract class AbstractHistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";
	
	private AbstractHistory history;
	
	public AbstractHistoryPage(String name, String path, HtmlLink parent, AbstractHistory history) {
		super(name, path, parent);
		this.history = history;
	}

	protected String getStatusHtml() {
		
		HtmlList list = new HtmlList("Status");
		list.add("Status", StatusManager.getStatusHtml(history.status));
		if(!history.description.isEmpty()){
			list.add("Description", history.description);
		}
		if(history.start != null){
			list.add("Start", DateTools.getTextDate(history.start));
		}
		if(history.start != null && history.end != null){
			list.add("Time", DateTools.getTimeDiff(history.start, history.end)+" '");
		}
		return list.getHtml();
	}
	
	protected String getArtifactHtml() {
		
		StringBuilder html = new StringBuilder();
		if(history.artifacts.size() > 0){
			html.append("<h3>Artifacts</h3>\n");
			html.append(getArtifactHtml(history.artifacts));
		}
		return html.toString();
	}

	private String getArtifactHtml(ArrayList<Artifact> artifacts) {
		
		StringBuilder html = new StringBuilder();
		html.append("<ul>\n");
		for(Artifact artifact : artifacts){
			html.append("<li>\n");
			html.append(artifact.toHtml()+"\n");
			if(artifact.childs.size() > 0){
				html.append(getArtifactHtml(artifact.childs));
			}
			html.append("</li>\n");
		}
		html.append("</ul>\n");
		return html.toString();
	}
	
	protected String getErrorHtml(){
		
		if(history.errors.size() > 0){
			HtmlList list = new HtmlList("Errors");
			list.setType(HtmlList.Type.OL);
			for(Error error : history.errors){
				list.add(null, error.getHtml());
			}
			return list.getHtml();
		}else{
			return "";
		}
	}
}
