package html;

import java.io.File;

import launch.StatusManager;
import logger.Logger;
import util.StringTools;
import data.LaunchHistory;
import data.OperationHistory;


public class LaunchHistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";
	
	private LaunchHistory history;
	
	public LaunchHistoryPage(String name, String path, HtmlLink parent, LaunchHistory history) {
		super(name, path, parent);
		this.history = history;
	}

	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		html.append(getStatusHtml());
		html.append(getOperationsHtml());
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
		HtmlLink logfile = new HtmlLink("Logfile", Logger.OUTPUT_FILE);
		list.add("Output", logfile.getHtml());
		return list.getHtml();
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
				table.addContentCell("<b>"+link.getHtml()+"</b>");
				table.addContentCell(operation.description);
				table.addContentCell(
						operation.start != null ? StringTools.getTextDate(operation.start) : ""
				);
				table.addContentCell(
						(operation.start != null && operation.end != null) ? 
								StringTools.getTimeDiff(operation.start, operation.end)+ " '" : ""
				);
				table.addContentCell(StatusManager.getStatusHtml(operation.status));
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}

}
