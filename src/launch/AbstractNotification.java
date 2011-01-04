package launch;

import java.util.ArrayList;
import java.util.Collections;

import data.Artifact;

import operation.IRepositoryOperation;

import launch.StatusManager.Status;
import logger.ILogConfig.Module;
import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;
import smtp.Mail;
import smtp.SmtpClient;
import util.StringTools;

public abstract class AbstractNotification {

	protected SmtpClient client;
	protected LaunchAgent launch;
	
	public AbstractNotification(SmtpClient client, LaunchAgent launch){
		
		this.client = client;
		this.launch = launch;
	}
	
	public Artifact performNotification() throws Exception {
		
		Mail mail = new Mail(getSubject());
		mail.from = client.getConfig().getSmtpAddress();
		mail.to = getToAdresses();
		mail.cc = getCcAdresses();
		mail.content = getContent();
		
		Artifact artifact = new Artifact(mail.subject, mail.getHtml(), "htm");
		if(isNotification()){
			client.send(mail, launch.getLogger());
			artifact.status = mail.isSend() ? Status.SUCCEED : Status.ERROR;
		}else{
			launch.getLogger().debug(Module.SMTP, "Notification canceled");
			artifact.status = Status.CANCEL;
		}
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
}
