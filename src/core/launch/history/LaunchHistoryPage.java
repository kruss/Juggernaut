package core.launch.history;


import java.io.File;

import core.html.HtmlLink;
import core.html.HtmlTable;
import core.launch.data.StatusManager;

import util.DateTools;
import util.StringTools;

public class LaunchHistoryPage extends AbstractHistoryPage {
	
	private static final int DESCRIPTION_MAX = 100;
	
	private LaunchHistory history;
	
	public LaunchHistoryPage(String name, String path, HtmlLink parent, LaunchHistory history) {
		super(name, path, parent, history);
		this.history = history;
	}

	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		html.append(getGeneralHtml());
		html.append(getOperationsHtml());
		html.append(getArtifactHtml());
		return html.toString();
	}
	
	private String getOperationsHtml() {
		
		if(history.operations.size() > 0){
			HtmlTable table = new HtmlTable("Operations");
			table.addHeaderCell("Operation", 150);
			table.addHeaderCell("Description", 250);
			table.addHeaderCell("Start", 100);
			table.addHeaderCell("Time", 75);
			table.addHeaderCell("Status", 100);
			for(OperationHistory operation : history.operations){
				HtmlLink link = new HtmlLink(operation.name, operation.id+File.separator+OUTPUT_FILE);
				table.addContentCell(operation.index+".) <b>"+link.getHtml()+"</b>");
				table.addContentCell(StringTools.border(operation.description, DESCRIPTION_MAX));
				table.addContentCell(
						operation.start != null ? DateTools.getTextDate(operation.start) : ""
				);
				table.addContentCell(
						(operation.start != null && operation.end != null) ? 
								DateTools.getTimeDiff(operation.start, operation.end)+ " '" : ""
				);
				table.addContentCell(StatusManager.getStatusHtml(operation.status));
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
}
