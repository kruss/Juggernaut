package core.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import util.DateTools;
import util.FileTools;
import util.IChangeListener;
import core.Constants;
import core.ISystemComponent;
import core.html.AbstractHtmlPage;
import core.html.HtmlLink;
import core.html.HtmlList;
import core.html.HtmlTable;
import core.launch.data.StatusManager;
import core.launch.data.StatusManager.Status;
import core.persistence.History;
import core.persistence.History.HistoryInfo;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

/** generates the history index */
public class HistoryIndex implements ISystemComponent, IChangeListener {

	private static final long DATE_THRESHOLD = 24 * 60 * 60 * 1000; // 1 day
	
	private History history;
	private Logger logger;
	private File folder;
	private Date date;
	
	public HistoryIndex(FileManager fileManager, History history, Logger logger){
		
		this.history = history;
		this.logger = logger;
		
		folder = fileManager.getHistoryFolder();
		date = null;
	}
	
	@Override
	public void init() throws Exception {
		create();
		history.addListener(this);
	}

	@Override
	public void shutdown() throws Exception {
		history.removeListener(this);
	}
	
	public synchronized void create() throws Exception {
		
		date = new Date();
		cleanup();
		createIndex();
		logger.debug(Module.COMMON, "create index: "+((new Date()).getTime()-date.getTime())+" ms");
	}
	
	private void cleanup() throws Exception {
		
		File[] files = folder.listFiles();
		for(File file : files){
			if(file.isFile() && file.getName().startsWith(Constants.INDEX_NAME)){
				FileTools.deleteFile(file.getAbsolutePath());
			}
		}
	}
	
	private void createIndex() throws Exception {
		
		IndexPage page = new IndexPage(
				Constants.APP_NAME+" [ Overview ]", 
				folder.getAbsolutePath()+File.separator+Constants.INDEX_NAME+".htm",
				null, 
				history.getHistoryNames()
		);
		page.create();
	}
	
	private void createSubIndex(String name, ArrayList<HistoryInfo> entries) throws Exception {
		
		HistoryPage page = new HistoryPage(
				"History [ "+name+" ]", 
				folder.getAbsolutePath()+File.separator+Constants.INDEX_NAME+"["+name.hashCode()+"].htm",
				new HtmlLink("&lt;&lt;", Constants.INDEX_NAME+".htm"), 
				entries
		);
		page.create();
	}
	
	private class IndexPage extends AbstractHtmlPage {
		
		private ArrayList<String> names;
		
		public IndexPage(String name, String path, HtmlLink parent, ArrayList<String> names) {
			super(name, path, parent);
			this.names = names;
			expires = true;
			refresh = true;
		}

		@Override
		public String getBody() {
			
			StringBuilder html = new StringBuilder();
			if(names.size() > 0){
				for(String name : names){
					try{ 
						// create launch sub-index
						ArrayList<HistoryInfo> entries = history.getHistoryInfo(name);
						createSubIndex(name, entries);
						// create launch overview
						HistoryInfo last = entries.get(0); // at least one !
						HtmlLink link = new HtmlLink(name, Constants.INDEX_NAME+"["+name.hashCode()+"].htm");
						html.append("<h3>Launch [ "+link.getHtml()+" ] - "+StatusManager.getStatusHtml(last.status)+"</h3>");
						ArrayList<HistoryInfo> latest = filterHistory(entries, date, DATE_THRESHOLD);
						if(latest.size() > 0){
							html.append(getHistoryHtml(null, latest));
						}
					}catch(Exception e){ 
						logger.error(Module.COMMON, e);
					}
				}
			}else{
				html.append("<i>empty</i>");
			}
			return html.toString();
		}
	}
	
	private class HistoryPage extends AbstractHtmlPage {

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
				html.append(getStatisticHtml("Info", entries));
				html.append(getHistoryHtml("Launches", entries));
			}else{
				html.append("<i>empty</i>");
			}
			return html.toString();
		}
	}
	
	private ArrayList<HistoryInfo> filterHistory(ArrayList<HistoryInfo> entries, Date date, long diff) {
		
		ArrayList<HistoryInfo> filter = new ArrayList<HistoryInfo>();
		for(HistoryInfo entry : entries){
			if(date.getTime() - diff > entry.start.getTime()){
				break;
			}
			filter.add(entry);
		}
		return filter;
	}
	
	private String getStatisticHtml(String title, ArrayList<HistoryInfo> entries) {

		HtmlList list = new HtmlList(title);
		list.addEntry("Ratio", getRatio(entries)+" %");
		list.addEntry("Time", getTime(entries)+" '");
		list.addEntry("Count", ""+entries.size());
		return list.getHtml();
	}

	private String getHistoryHtml(String title, ArrayList<HistoryInfo> entries) {

		HtmlTable table = new HtmlTable(title);
		table.addHeaderCell("Launch", 150);
		table.addHeaderCell("Trigger", 250);
		table.addHeaderCell("Start", 100);
		table.addHeaderCell("Time", 75);
		table.addHeaderCell("Status", 100);
		for(HistoryInfo entry : entries){
			HtmlLink link = new HtmlLink(entry.name, entry.start.getTime()+File.separator+Constants.INDEX_NAME+".htm");
			table.addContentCell("<b>"+link.getHtml()+"</b>");
			table.addContentCell(entry.trigger);
			table.addContentCell(DateTools.getTextDate(entry.start));
			table.addContentCell(DateTools.getTimeDiff(entry.start, entry.end)+ " '");
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

	@Override
	public void changed(Object object) {
		
		if(object instanceof History){
			try{
				create();
			}catch(Exception e){
				logger.error(Module.COMMON, e);
			}
		}
	}
}
