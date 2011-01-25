package core.launch.notification;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;



import core.html.HtmlLink;
import core.html.HtmlList;
import core.html.HtmlTable;
import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.Error;
import core.launch.data.StatusManager;
import core.launch.data.StatusManager.Status;
import core.launch.history.HistoryPage;
import core.launch.history.LaunchHistory;
import core.launch.history.OperationHistory;
import core.launch.operation.AbstractOperation;
import core.launch.operation.IRepositoryOperation;
import core.launch.repository.IRepositoryClient.CommitInfo;
import core.launch.repository.IRepositoryClient.HistoryInfo;
import core.persistence.History;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;
import core.runtime.smtp.Mail;


import util.DateTools;
import util.SystemTools;

public class Notification {

	private static final int MAX_HISTORY = 3;
	
	private History history;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchAgent launch;
	private LaunchHistory previous;
	
	public Notification(
			History history, ISmtpClient smtpClient, IHttpServer httpServer, LaunchAgent launch
	){
		this.history = history;
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.launch = launch;
		this.previous = history.getLatest(launch.getConfig().getId());
	}
	
	public Artifact performNotification() {
		
		Mail mail = new Mail(getSubject());
		mail.from = smtpClient.getConfig().getSmtpAddress();
		mail.to = getToAdresses();
		mail.cc = getCcAdresses();
		mail.content = getContent();
		Status status = null;
		
		if(isNotificationEnabled()){
			try{ 
				smtpClient.send(mail, launch.getLogger());
			}catch(Exception e){
				launch.getLogger().error(Module.SMTP, e);
				status = Status.ERROR;
			}
		}else{
			launch.getLogger().debug(Module.SMTP, "Notification NOT enabled");
			status = Status.CANCEL;
		}
		
		Artifact artifact = new Artifact(getClass().getSimpleName(), mail.getHtml(), "htm");
		artifact.status = status;
		return artifact;
	}
	
	private String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - "+launch.getStatusManager().getStatus().toString();
	}
	
	private ArrayList<String> getToAdresses() {
		return getAdministratorAdresses();
	}
	
	private ArrayList<String> getCcAdresses() {
		
		if(isCommitterNotificationRequired()){
			return getComitterAddresses();
		}else{
			return new ArrayList<String>();
		}
	}

	private String getContent() {
		
		StringBuilder html = new StringBuilder();
		html.append("<h1>Launch ["+launch.getConfig().getName()+"]</h1>\n");
		html.append(getGeneralHtml());
		html.append(getOperationsHtml());
		html.append(getErrorHtml());
		html.append(getCommitterHtml());
		html.append(getHistoryHtml());
		html.append(getUserHtml());
		html.append(getLinkHtml());
		return html.toString();
	}

	private String getGeneralHtml() {
		
		HtmlList list = new HtmlList("Info");
		Status currentStatus = launch.getStatusManager().getStatus();
		Status lastStatus = previous != null ? previous.status : null;
		if(lastStatus != null && lastStatus != currentStatus){
			list.addEntry("Status", StatusManager.getStatusHtml(lastStatus)+" -> "+StatusManager.getStatusHtml(currentStatus));
		}else{
			list.addEntry("Status", StatusManager.getStatusHtml(currentStatus));
		}
		String description = launch.getConfig().getDescription();
		if(!description.isEmpty()){
			list.addEntry("Description", description);
		}
		String trigger = launch.getTrigger();
		if(!trigger.isEmpty()){
			list.addEntry("Trigger", trigger);
		}
		Date start = launch.getStatusManager().getStart();
		if(start != null){
			list.addEntry("Date", DateTools.getTextDate(start));
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
				table.addContentCell(operation.getIndex()+".) <b>"+operation.getConfig().getName()+"</b>");
				table.addContentCell(operation.getDescription());
				Status currentStatus = operation.getStatusManager().getStatus();
				OperationHistory operationHistory = previous != null ? previous.getOperation(operation.getConfig().getId()) : null;
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
	
	private String getErrorHtml() {
		
		StringBuilder html = new StringBuilder();
		
		ArrayList<Error> newErrors = getNewErrors();
		if(newErrors.size() > 0){
			HtmlList list = new HtmlList("Errors (new)");
			for(Error error : newErrors){
				list.addEntry(error.origin, error.getHtml());
			}
			html.append(list.getHtml());
		}
		
		ArrayList<Error> oldErrors = getOldErrors();
		if(oldErrors.size() > 0){
			HtmlList list = new HtmlList("Errors (old)");
			for(Error error : oldErrors){
				list.addEntry(error.origin, error.getHtml());
			}
			html.append(list.getHtml());
		}
		
		return html.toString();
	}

	private ArrayList<Error> getNewErrors() {
		if(previous != null){
			return Error.getDelta(launch.getErrors(), previous.getErrors());
		}else{
			return launch.getErrors();
		}
	}

	private ArrayList<Error> getOldErrors() {
		if(previous != null){
			return Error.getMatch(launch.getErrors(), previous.getErrors());
		}else{
			return new ArrayList<Error>();
		}
	}
	
	private String getCommitterHtml() {
		
		ArrayList<IRepositoryOperation> repositories = launch.getRepositoryOperations();
		if(hasCommitter()){
			HtmlList list = new HtmlList("Commits");
			if(!isCommitterNotificationRequired()){
				list.setDescription("<font color=blue>!!! Committer NOT being notified !!!</font>");
			}
			for(IRepositoryOperation repository : repositories){
				HistoryInfo history = repository.getHistory();
				if(history != null){
					HtmlList commits = new HtmlList(null);
					for(CommitInfo commit : history.commits){
						commits.addEntry(null, commit.toString());
					}
					String revision1 = history.revision1;
					String revision2 = history.revision2;
					list.addEntry(
							repository.getUrl()+" ("+(revision1.equals(revision2) ? revision2 : revision1+" - "+revision2)+")", 
							commits.getHtml()
					);
				}
			}
			return list.getHtml();
		}else{			
			return "";
		}
	}

	private String getHistoryHtml() {
		
		if(previous != null){
			HtmlList list = new HtmlList("History");
			LaunchHistory entry = previous;
			while(entry != null && list.getSize() < MAX_HISTORY){
				list.addEntry(DateTools.getTextDate(entry.start), StatusManager.getStatusHtml(entry.status));
				entry = history.getPrevious(entry);
			}
			return list.getHtml();
		}else{			
			return "";
		}
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
		
		if(httpServer.getConfig().isHttpServer()){
			try{
				String url = 
					"http://"+SystemTools.getHostName()+":"+httpServer.getConfig().getHttpPort()+
					"/"+launch.getStatusManager().getStart().getTime()+"/"+HistoryPage.OUTPUT_FILE;
				HtmlLink link = new HtmlLink(url, url);
				link.setExtern(true);
				HtmlList list = new HtmlList("Output");
				list.addEntry("Logfile", link.getHtml());
				return list.getHtml();
			}catch(Exception e){
				launch.getLogger().error(Module.HTTP, e);
				return "";
			}
		}else{
			return "";
		}
	}
	
	private boolean isNotificationEnabled(){
		return smtpClient.getConfig().isNotification() && launch.getConfig().isNotification();
	}
	
	private boolean isCommitterNotificationRequired(){	
		return hasCommitter() && isCommitterThresholdValid() && isCommitterStatusValid();
	}
	
	private boolean hasCommitter(){
		return getComitterAddresses().size() > 0;
	}
	
	private boolean isCommitterThresholdValid(){
		
		int threshold = launch.getConfig().getCommitterThreshold();
		int committers = getComitterAddresses().size();
		return threshold >= committers;
	}
	
	private boolean isCommitterStatusValid(){
		
		Status currentStatus = launch.getStatusManager().getStatus();
		Status lastStatus = previous != null ? previous.status : null;
		return currentStatus != Status.FAILURE && (lastStatus == null || lastStatus != Status.FAILURE);
	}
	
	private ArrayList<String> getAdministratorAdresses() {
		
		ArrayList<String> admins = new ArrayList<String>();
		for(String addr : smtpClient.getConfig().getAdministratorAddresses()){
			if(!admins.contains(addr)){
				admins.add(addr);
			}
		}
		for(String addr : launch.getConfig().getAdministratorAddresses()){
			if(!admins.contains(addr)){
				admins.add(addr);
			}
		}
		Collections.sort(admins);
		return admins;
	}
	
	private ArrayList<String> getComitterAddresses() {
		
		ArrayList<String> committers = new ArrayList<String>();
		for(IRepositoryOperation repository : launch.getRepositoryOperations()){
				HistoryInfo history = repository.getHistory();
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
