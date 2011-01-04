package launch;

import html.HistoryPage;
import html.HtmlLink;
import html.HtmlList;
import http.IHttpServer;

import java.util.ArrayList;
import java.util.Collections;

import core.Constants;
import data.Artifact;

import operation.IRepositoryOperation;

import launch.StatusManager.Status;
import logger.ILogConfig.Module;
import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;
import smtp.Mail;
import smtp.ISmtpClient;
import util.StringTools;
import util.SystemTools;

public abstract class AbstractNotification {

	protected ISmtpClient client;
	protected IHttpServer server;
	protected LaunchAgent launch;
	
	public AbstractNotification(
			ISmtpClient client, IHttpServer server, LaunchAgent launch
	){
		
		this.client = client;
		this.server = server;
		this.launch = launch;
	}
	
	public Artifact performNotification() {
		
		Mail mail = new Mail(getSubject());
		mail.from = client.getConfig().getSmtpAddress();
		mail.to = getToAdresses();
		mail.cc = getCcAdresses();
		mail.content = getContent();
		
		Status status = null;
		if(isNotification()){
			try{ 
				client.send(mail, launch.getLogger());
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
		return client.getConfig().isNotification() && launch.getConfig().isNotification();
	}

	protected abstract String getSubject();
	protected abstract ArrayList<String> getToAdresses();
	protected abstract ArrayList<String> getCcAdresses();
	protected abstract String getContent();

	protected ArrayList<String> getAdministratorAdresses() {
		
		ArrayList<String> admins = new ArrayList<String>();
		for(String addr : client.getConfig().getAdministrators()){
			StringTools.addUnique(admins, addr);
		}
		for(String addr : launch.getConfig().getAdministrators()){
			StringTools.addUnique(admins, addr);
		}
		Collections.sort(admins);
		return admins;
	}
	
	protected ArrayList<String> getComitterAddresses() {
		
		ArrayList<String> committers = new ArrayList<String>();
		for(IRepositoryOperation operation : launch.getRepositoryOperations()){
				HistoryInfo history = operation.getHistory();
				for(CommitInfo commit : history.commits){
					if(!committers.contains(commit.author)){
						committers.add(commit.author);
					}
				}
		}
		Collections.sort(committers);
		return committers;
	}
	
	protected String getUserInfo() {
		
		String message = launch.getConfig().getSmtpMessage();
		if(!message.isEmpty()){
			StringBuilder html = new StringBuilder();
			html.append("<h2>Info</h2>\n");
			html.append("<p>\n");
			html.append(message.replaceAll("\\n", "<br>"));
			html.append("</p>\n");
			return html.toString();
		}else{
			return "";
		}
	}
	
	protected String getLinkInfo() {
		
		if(server.isRunning()){
			try{
				String url = 
					"http://"+SystemTools.getHostName()+":"+server.getPort()+
					"/"+launch.getStatusManager().getStart().getTime()+"/"+HistoryPage.OUTPUT_FILE;
				HtmlLink link = new HtmlLink(url, url);
				link.setExtern(true);
				HtmlList list = new HtmlList("Output");
				list.add(Constants.APP_NAME, link.getHtml());
				return list.getHtml();
			}catch(Exception e){
				launch.getLogger().error(Module.HTTP, e);
			}
		}
		return "";
	}
}
