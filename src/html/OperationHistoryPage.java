package html;

import launch.StatusManager;
import util.StringTools;
import data.OperationHistory;

public class OperationHistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";
	
	private OperationHistory history;
	
	public OperationHistoryPage(String name, String path, HtmlLink parent, OperationHistory history) {
		super(name, path, parent);
		this.history = history;
	}

	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		html.append(getStatusHtml());
		return html.toString();
	}
	
	private String getStatusHtml() {
		
		HtmlList list = new HtmlList("Status");
		list.add("Status", StatusManager.getStatusHtml(history.status));
		if(!history.description.isEmpty()){
			list.add("Description", history.description);
		}
		if(history.start != null){
			list.add("Start", StringTools.getTextDate(history.start));
		}
		if(history.start != null && history.end != null){
			list.add("Time", StringTools.getTimeDiff(history.start, history.end)+" '");
		}
		return list.getHtml();
	}
}
