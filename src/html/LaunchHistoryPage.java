package html;

import java.io.File;

import launch.StatusManager;
import util.DateTools;
import data.LaunchHistory;
import data.OperationHistory;

public class LaunchHistoryPage extends AbstractHistoryPage {
	
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
				table.addContentCell(operation.description);
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
