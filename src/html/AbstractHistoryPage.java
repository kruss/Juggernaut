package html;

import java.util.ArrayList;

import launch.StatusManager;
import util.DateTools;
import data.AbstractHistory;
import data.Artifact;
import data.LaunchHistory;

public abstract class AbstractHistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";
	
	private AbstractHistory history;
	
	public AbstractHistoryPage(String name, String path, HtmlLink parent, AbstractHistory history) {
		super(name, path, parent);
		this.history = history;
	}

	protected String getGeneralHtml() {
		
		HtmlList list = new HtmlList("Info");
		list.addEntry("Status", StatusManager.getStatusHtml(history.status));
		if(!history.description.isEmpty()){
			list.addEntry("Description", history.description);
		}
		if(history instanceof LaunchHistory){
			if(!((LaunchHistory)history).trigger.isEmpty()){
				list.addEntry("Trigger", ((LaunchHistory)history).trigger);
			}
		}
		if(history.start != null){
			list.addEntry("Start", DateTools.getTextDate(history.start));
		}
		if(history.start != null && history.end != null){
			list.addEntry("Time", DateTools.getTimeDiff(history.start, history.end)+" '");
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
}
