package html;

import java.io.File;
import java.util.ArrayList;

import launch.StatusManager;

import util.StringTools;

import core.Constants;
import core.History;
import core.History.HistoryInfo;

public class HistoryPage extends AbstractHtmlPage {

	public static final String OUTPUT_FILE = "index.htm";

	private ArrayList<HistoryInfo> entries;
	
	public HistoryPage(History history, String path) {
		super(Constants.APP_NAME+" [ History ]", path, null);
		entries = history.getHistoryInfo();
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
				table.addContentCell("<b>"+link.getHtml()+"</b>");
				table.addContentCell(entry.trigger);
				table.addContentCell(
						entry.start != null ? StringTools.getTextDate(entry.start) : ""
				);
				table.addContentCell(
						(entry.start != null && entry.end != null) ? 
						StringTools.getTimeDiff(entry.start, entry.end)+ " '" : ""
				);
				table.addContentCell(StatusManager.getHtml(entry.status));
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
}
