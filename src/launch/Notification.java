package launch;

import html.HistoryPage;
import html.HtmlLink;
import html.HtmlList;
import html.HtmlTable;
import html.HtmlList.Type;
import http.IHttpServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import operation.IRepositoryOperation;

import launch.StatusManager.Status;
import logger.ILogConfig.Module;

import core.History;

import data.AbstractOperation;
import data.Artifact;
import data.LaunchHistory;
import data.OperationHistory;

import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;
import smtp.ISmtpClient;
import smtp.Mail;
import util.DateTools;
import util.StringTools;
import util.SystemTools;
import data.Error;

public class Notification {

	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchAgent launch;
	private LaunchHistory launchHistory;
	
	public Notification(
			History history, ISmtpClient smtpClient, IHttpServer httpServer, LaunchAgent launch
	){
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.launch = launch;
		this.launchHistory = history.getLatest(launch.getConfig().getId());
	}
	
	public Artifact performNotification() {
		
		Mail mail = new Mail(getSubject());
		mail.from = smtpClient.getConfig().getSmtpAddress();
		mail.to = getToAdresses();
		mail.cc = getCcAdresses();
		mail.content = getContent();
		Status status = null;
		
		if(isNotification()){
			try{ 
				smtpClient.send(mail, launch.getLogger());
			}catch(Exception e){
				launch.getLogger().error(Module.SMTP, e);
				status = Status.ERROR;
			}
		}else{
			launch.getLogger().debug(Module.SMTP, "Notification canceled");
			status = Status.CANCEL;
		}
		
		Artifact artifact = new Artifact(mail.subject, mail.getHtml(), "htm");
		artifact.status = status;
		return artifact;
	}
	
	private boolean isNotification(){
		return smtpClient.getConfig().isNotification() && launch.getConfig().isNotification();
	}
	
	private boolean isCommitterNotification(){
		
		int committers = getComitterAddresses().size();
		int threshold = launch.getConfig().getCommitterThreshold();
		return 
			committers > 0 && threshold >= committers && launch.getStatusManager().getStatus() != Status.FAILURE;
	}
	
	private String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - "+launch.getStatusManager().getStatus().toString();
	}
	
	private ArrayList<String> getToAdresses() {
		return getAdministratorAdresses();
	}
	
	private ArrayList<String> getCcAdresses() {
		
		if(isCommitterNotification()){
			return getComitterAddresses();
		}else{
			return new ArrayList<String>();
		}
	}

	private String getContent() {
		
		StringBuilder html = new StringBuilder();
		html.append("<h1>Launch ["+launch.getConfig().getName()+"]</h1>\n");
		html.append(getWarningsHtml());
		html.append(getGeneralHtml());
		html.append(getOperationsHtml());
		html.append(getCommitterHtml());
		html.append(getErrorHtml());
		html.append(getUserHtml());
		html.append(getLinkHtml());
		return html.toString();
	}
	
	private String getWarningsHtml() {
		
		StringBuilder html = new StringBuilder();
		int committers = getComitterAddresses().size();
		int threshold = launch.getConfig().getCommitterThreshold();
		if(committers > 0 && threshold > 0 && !isCommitterNotification()){
			html.append("<p><b><font color=blue>\n");
			html.append(
					"!!! Committers NOT being notified due "+(
							threshold < committers ? "Threshold reached" : "invalid Status")+
					" !!!\n"
			);
			html.append("</font></b></p>\n");
		}
		return html.toString();
	}

	private String getGeneralHtml() {
		
		HtmlList list = new HtmlList("Info");
		Status currentStatus = launch.getStatusManager().getStatus();
		Status lastStatus = launchHistory != null ? launchHistory.status : null;
		if(lastStatus != null && lastStatus != currentStatus){
			list.add("Status", StatusManager.getStatusHtml(lastStatus)+" -> "+StatusManager.getStatusHtml(currentStatus));
		}else{
			list.add("Status", StatusManager.getStatusHtml(currentStatus));
		}
		String description = launch.getConfig().getDescription();
		if(!description.isEmpty()){
			list.add("Description", description);
		}
		String trigger = launch.getTrigger();
		if(!trigger.isEmpty()){
			list.add("Trigger", trigger);
		}
		Date currentStart = launch.getStatusManager().getStart();
		if(currentStart != null){
			list.add("Date", DateTools.getTextDate(currentStart));
		}
		Date lastStart = launchHistory != null ? launchHistory.start : null;
		if(lastStart != null){
			list.add("Last", "<i>"+DateTools.getTextDate(lastStart)+"</i>");
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
				Status currentStatus = operation.getStatusManager().getStatus();
				OperationHistory operationHistory = launchHistory != null ? launchHistory.getOperation(operation.getConfig().getId()) : null;
				Status lastStatus = operationHistory != null ? operationHistory.status : null;
				if(lastStatus != null && lastStatus != currentStatus){
					table.addContentCell(
							StatusManager.getStatusHtml(lastStatus)+" -> "+StatusManager.getStatusHtml(currentStatus)
					);
				}else{
					table.addContentCell(
							StatusManager.getStatusHtml(currentStatus)
					);
				}
			}
			return table.getHtml();
		}else{
			return "<i>empty</i>";
		}
	}
	
	private String getCommitterHtml() {
		
		ArrayList<IRepositoryOperation> repositories = launch.getRepositoryOperations();
		if(repositories.size() > 0){
			HtmlList list = new HtmlList("Commits");
			for(IRepositoryOperation repository : repositories){
				String url = repository.getUrl();
				HistoryInfo info = repository.getHistory();
				StringBuilder commits = new StringBuilder();
				for(CommitInfo commit : info.commits){
					commits.append("<li>"+commit.toString()+"</li>\n");
				}
				list.add(
						url+" ("+info.revision1+" - "+info.revision2+")", 
						"<ul>"+commits.toString()+"</ul>"
				);
			}
			return list.getHtml();
		}
		return "";
	}
	
	private String getErrorHtml() {
		
		StringBuilder html = new StringBuilder();
		
		ArrayList<Error> newErrors = getNewErrors();
		if(newErrors.size() > 0){
			HtmlList list = new HtmlList("New Errors");
			list.setType(Type.OL);
			for(Error error : newErrors){
				list.add(null, error.getHtml());
			}
			html.append(list.getHtml());
		}
		
		ArrayList<Error> oldErrors = getOldErrors();
		if(oldErrors.size() > 0){
			HtmlList list = new HtmlList("Old Errors");
			list.setType(Type.OL);
			for(Error error : oldErrors){
				list.add(null, error.getHtml());
			}
			html.append(list.getHtml());
		}
		
		return html.toString();
	}

	private ArrayList<Error> getNewErrors() {
		if(launchHistory != null){
			return delta(launch.getErrors(), launchHistory.getErrors());
		}else{
			return launch.getErrors();
		}
	}


	private ArrayList<Error> getOldErrors() {
		if(launchHistory != null){
			return delta(launchHistory.getErrors(), launch.getErrors());
		}else{
			return new ArrayList<Error>();
		}
	}
	
	/** return only items which are in list1 but not in list2 */
	private ArrayList<Error> delta(ArrayList<Error> list1, ArrayList<Error> list2) {
		
		ArrayList<Error> delta = new ArrayList<Error>();
		for(Error error1 : list1){
			boolean found = false;
			for(Error error2 : list2){
				if(error1.getHash() == error2.getHash()){
					found = true;
					break;
				}
			}
			if(!found){
				delta.add(error1);
			}
		}
		return delta;
	}

	private String getUserHtml() {
		
		String message = launch.getConfig().getSmtpMessage();
		if(!message.isEmpty()){
			StringBuilder html = new StringBuilder();
			html.append("<h3>Message</h3>\n");
			html.append("<p>\n");
			html.append(message.replaceAll("\\n", "<br>"));
			html.append("</p>\n");
			return html.toString();
		}else{
			return "";
		}
	}
	
	private String getLinkHtml() {
		
		if(httpServer.isRunning()){
			try{
				String url = 
					"http://"+SystemTools.getHostName()+":"+httpServer.getPort()+
					"/"+launch.getStatusManager().getStart().getTime()+"/"+HistoryPage.OUTPUT_FILE;
				HtmlLink link = new HtmlLink(url, url);
				link.setExtern(true);
				HtmlList list = new HtmlList("Output");
				list.add("Logfile", link.getHtml());
				return list.getHtml();
			}catch(Exception e){
				launch.getLogger().error(Module.HTTP, e);
			}
		}
		return "";
	}
	
	private ArrayList<String> getAdministratorAdresses() {
		
		ArrayList<String> admins = new ArrayList<String>();
		for(String addr : smtpClient.getConfig().getAdministratorAddresses()){
			StringTools.addUnique(admins, addr);
		}
		for(String addr : launch.getConfig().getAdministratorAddresses()){
			StringTools.addUnique(admins, addr);
		}
		Collections.sort(admins);
		return admins;
	}
	
	private ArrayList<String> getComitterAddresses() {
		
		ArrayList<String> committers = new ArrayList<String>();
		for(IRepositoryOperation operation : launch.getRepositoryOperations()){
				HistoryInfo history = operation.getHistory();
				if(history != null){
					for(CommitInfo commit : history.commits){
						if(!committers.contains(commit.author)){
							committers.add(commit.author);
						}
					}
				}
		}
		Collections.sort(committers);
		return committers;
	}
}
