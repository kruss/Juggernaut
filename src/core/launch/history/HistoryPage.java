package core.launch.history;


import java.io.File;
import java.util.ArrayList;


import util.DateTools;

import core.Constants;
import core.html.AbstractHtmlPage;
import core.html.HtmlLink;
import core.html.HtmlTable;
import core.launch.data.StatusManager;
import core.persistence.History;
import core.persistence.History.HistoryInfo;

public class HistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";

	private ArrayList<HistoryInfo> entries;
	
	public HistoryPage(History history, String path) {
		super(Constants.APP_NAME+" [ History ]", path, null);
		
		entries = history.getHistoryInfo();
		
		expires = true;
		refresh = true;
	}
	
	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		html.append(getHistoryHtml());
		return html.toString();
	}

	private String getHistoryHtml() {

		if(entries.size() > 0){
			HtmlTable table = new HtmlTable(null);
			table.addHeaderCell("Launch", 150);
			table.addHeaderCell("Trigger", 250);
			table.addHeaderCell("Start", 100);
			table.addHeaderCell("Time", 75);
			table.addHeaderCell("Status", 100);
			for(HistoryInfo entry : entries){
				HtmlLink link = new HtmlLink(entry.name, entry.start.getTime()+File.separator+OUTPUT_FILE);
				link.setExtern(true);
				table.addContentCell("<b>"+link.getHtml()+"</b>");
				table.addContentCell(entry.trigger);
				table.addContentCell(
						entry.start != null ? DateTools.getTextDate(entry.start) : ""
				);
				table.addContentCell(
						(entry.start != null && entry.end != null) ? 
								DateTools.getTimeDiff(entry.start, entry.end)+ " '" : ""
				);
				table.addContentCell(StatusManager.getStatusHtml(entry.status));
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
}
