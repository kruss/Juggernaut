package core.launch.history;


import java.io.File;
import java.util.ArrayList;


import util.DateTools;

import core.html.AbstractHtmlPage;
import core.html.HtmlLink;
import core.html.HtmlList;
import core.html.HtmlTable;
import core.launch.data.StatusManager;
import core.launch.data.StatusManager.Status;
import core.persistence.History.HistoryInfo;

public class HistoryPage extends AbstractHtmlPage {

	private ArrayList<HistoryInfo> entries;
	
	public HistoryPage(String name, String path, HtmlLink parent, ArrayList<HistoryInfo> entries) {
		super(name, path, parent);
		
		this.entries = entries;
		
		expires = true;
		refresh = true;
	}
	
	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		if(entries.size() > 0){
			html.append(getStatisticHtml());
			html.append(getHistoryHtml());
		}else{
			html.append("<i>empty</i>");
		}
		return html.toString();
	}

	private String getStatisticHtml() {

		HtmlList list = new HtmlList("Info");
		list.addEntry("Ratio", getRatio(entries)+" %");
		list.addEntry("Av. Time", getTime(entries)+" '");
		list.addEntry("Items", ""+entries.size());
		return list.getHtml();
	}

	private String getHistoryHtml() {

		HtmlTable table = new HtmlTable("Launches");
		table.addHeaderCell("Launch", 150);
		table.addHeaderCell("Trigger", 250);
		table.addHeaderCell("Start", 100);
		table.addHeaderCell("Time", 75);
		table.addHeaderCell("Status", 100);
		for(HistoryInfo entry : entries){
			HtmlLink link = new HtmlLink(entry.name, entry.start.getTime()+File.separator+LaunchHistoryPage.OUTPUT_FILE);
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
	}

	/** get average succeed ratio */
	private int getRatio(ArrayList<HistoryInfo> entries) {
		
		int succeed = 0;
		int total = 0;
		for(HistoryInfo entry : entries){
			if(entry.status == Status.SUCCEED){
				succeed++;
			}
			if(entry.status != Status.CANCEL){
				total++;
			}
		}
		return (int)Math.round(((double)succeed / (double)total) * 100);
	}
	
	/** get average time in minutes */
	private int getTime(ArrayList<HistoryInfo> entries) {

		int time = 0;
		int total = 0;
		for(HistoryInfo entry : entries){
			if(entry.status != Status.CANCEL){
				time += DateTools.getTimeDiff(entry.start, entry.end);
				total++;
			}
		}
		return (int)Math.round(((double)time / (double)total));
	}
}
