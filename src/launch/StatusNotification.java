package launch;

import html.HtmlList;
import html.HtmlTable;
import http.IHttpServer;

import java.util.ArrayList;
import java.util.Date;

import data.AbstractOperation;

import smtp.ISmtpClient;
import util.DateTools;

public class StatusNotification extends AbstractNotification {

	public StatusNotification(
			ISmtpClient client, IHttpServer server, LaunchAgent launch
	) {
		super(client, server, launch);
	}
	
	@Override
	protected String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - Status";
	}
	
	@Override
	protected ArrayList<String> getToAdresses() {
		return getAdministratorAdresses();
	}
	
	@Override
	protected ArrayList<String> getCcAdresses() {
		return new ArrayList<String>();
	}

	@Override
	protected String getContent() {
		
		StringBuilder html = new StringBuilder();
		html.append("<h1>Launch ["+launch.getConfig().getName()+"]</h1>\n");
		// TODO show status-transitions
		html.append(getStatusHtml());
		html.append(getOperationsHtml());
		html.append(getUserInfo());
		html.append(getLinkInfo());
		return html.toString();
	}
	
	// TODO use the launch-history for mail-contents
	private String getStatusHtml() {
		
		HtmlList list = new HtmlList("Status");
		list.add("Status", StatusManager.getStatusHtml(launch.getStatusManager().getStatus()));
		String trigger = launch.getTrigger();
		if(!trigger.isEmpty()){
			list.add("Trigger", trigger);
		}
		String description = launch.getConfig().getDescription();
		if(!description.isEmpty()){
			list.add("Description", description);
		}
		Date start = launch.getStatusManager().getStart();
		if(start != null){
			list.add("Start", DateTools.getTextDate(start));
		}
		Date end = launch.getStatusManager().getEnd();
		if(start != null && end != null){
			list.add("Time", DateTools.getTimeDiff(start, end)+" '");
		}
		return list.getHtml();
	}
	
	// TODO use the launch-history for mail-contents
	private String getOperationsHtml() {
		
		if(launch.getOperations().size() > 0){
			HtmlTable table = new HtmlTable("Operations");
			table.addHeaderCell("Operation", 150);
			table.addHeaderCell("Description", 250);
			table.addHeaderCell("Start", 100);
			table.addHeaderCell("Time", 75);
			table.addHeaderCell("Status", 100);
			for(AbstractOperation operation : launch.getOperations()){
				table.addContentCell("<b>"+operation.getConfig().getName()+"</b>");
				table.addContentCell(operation.getDescription());
				Date start = operation.getStatusManager().getStart();
				table.addContentCell(
						start != null ? DateTools.getTextDate(start) : ""
				);
				Date end = operation.getStatusManager().getStart();
				table.addContentCell(
						(start != null && end != null) ? 
								DateTools.getTimeDiff(start, end)+ " '" : ""
				);
				table.addContentCell(StatusManager.getStatusHtml(operation.getStatusManager().getStatus()));
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
}
