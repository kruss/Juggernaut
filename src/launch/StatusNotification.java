package launch;

import html.HtmlList;
import html.HtmlTable;
import http.IHttpServer;

import java.util.ArrayList;
import java.util.Date;

import launch.StatusManager.Status;

import core.History;

import data.AbstractOperation;
import data.OperationHistory;

import smtp.ISmtpClient;
import util.DateTools;

public class StatusNotification extends AbstractNotification {

	public StatusNotification(
			History history, ISmtpClient client, IHttpServer server, LaunchAgent launch
	) {
		super(history, client, server, launch);
	}
	
	@Override
	protected String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - "+launch.getStatusManager().getStatus().toString();
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
		html.append(getGeneralHtml());
		html.append(getOperationsHtml());
		html.append(getUserHtml());
		html.append(getLinkHtml());
		return html.toString();
	}
	
	private String getGeneralHtml() {
		
		HtmlList list = new HtmlList("Info");
		Status current = launch.getStatusManager().getStatus();
		Status last = launchHistory != null ? launchHistory.status : null;
		if(last != null && last != current){
			list.add("Status", StatusManager.getStatusHtml(last)+" -> "+StatusManager.getStatusHtml(current));
		}else{
			list.add("Status", StatusManager.getStatusHtml(current));
		}
		String description = launch.getConfig().getDescription();
		if(!description.isEmpty()){
			list.add("Description", description);
		}
		String trigger = launch.getTrigger();
		if(!trigger.isEmpty()){
			list.add("Trigger", trigger);
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
	
	private String getOperationsHtml() {
		
		if(launch.getOperations().size() > 0){
			HtmlTable table = new HtmlTable("Operations");
			table.addHeaderCell("Operation", 150);
			table.addHeaderCell("Description", 250);
			table.addHeaderCell("Status", 100);
			for(AbstractOperation operation : launch.getOperations()){
				table.addContentCell("<b>"+operation.getConfig().getName()+"</b>");
				table.addContentCell(operation.getConfig().getDescription());
				Status current = operation.getStatusManager().getStatus();
				OperationHistory operationHistory = launchHistory != null ? launchHistory.getOperation(operation.getConfig().getId()) : null;
				Status last = operationHistory != null ? operationHistory.status : null;
				if(last != null && last != current){
					table.addContentCell(
							StatusManager.getStatusHtml(last)+" -> "+StatusManager.getStatusHtml(current)
					);
				}else{
					table.addContentCell(
							StatusManager.getStatusHtml(current)
					);
				}
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
}
