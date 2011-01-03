package launch;

import java.util.ArrayList;
import java.util.Collections;

import core.Cache;
import core.Constants;

import launch.StatusManager.Status;
import data.Error;
import logger.ILogConfig.Module;

import data.Artifact;

import operation.IRepositoryOperation;
import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;
import smtp.Mail;
import smtp.SmtpClient;
import smtp.ISmtpConfig.NotificationMode;
import util.StringTools;


/** performs the notification for a launch */
// TODO add checks to send only email when status change since last run or new errors
public class LaunchNotification {

	private enum Property { STATUS, ERRORS };
	
	private SmtpClient client;
	private LaunchAgent launch;
	private Cache cache;
	
	public LaunchNotification(
			SmtpClient client, Cache cache, LaunchAgent launch
	){
		this.client = client;
		this.launch = launch;
		this.cache = cache;
	}

	public void performNotification() throws Exception {
		
		if(isNotificationEnabled()){
			if(isNotificationRequired()){
				Mail mail = createMail();
				Artifact artifact = new Artifact("Notification", mail.getHtml(), "html");
				try{
					client.send(mail, launch.getLogger());
				}catch(Exception e){
					artifact.status = Status.ERROR;
					throw e;
				}finally{				
					launch.getArtifacts().add(artifact);
				}
			}else{
				launch.getLogger().debug(Module.SMTP, "Notification NOT required");
			}
		}else{
			launch.getLogger().debug(Module.SMTP, "Notification NOT enabled");
		}
		setLastStatus();
		setErrorsHash();
	}

	private boolean isNotificationEnabled(){
		return client.isReady() && (isNotifyAdmins() || isNotifyCommitters());
	}
	
	private boolean isNotifyAdmins() {
		return 
			(client.getConfig().getNotificationMode() == NotificationMode.ADMINSTRATORS &&
			launch.getConfig().getNotificationMode() == NotificationMode.ADMINSTRATORS) 
			||
			(client.getConfig().getNotificationMode() == NotificationMode.COMMITTER &&
			launch.getConfig().getNotificationMode() == NotificationMode.ADMINSTRATORS) ;
	}
	
	private boolean isNotifyCommitters() {
		return 
			client.getConfig().getNotificationMode() == NotificationMode.COMMITTER &&
			launch.getConfig().getNotificationMode() == NotificationMode.COMMITTER;
	}
	
	private boolean isNotificationRequired() {
		return isStatusChanged() || isErrorsChanged();
	}

	private boolean isStatusChanged() {
		
		Status last = getLastStatus();
		Status current = launch.getStatusManager().getStatus();
		return (last == null) || (last != current);
	}
	
	private void setLastStatus(){
		cache.addProperty(
				launch.getConfig().getId(), Property.STATUS.toString(), launch.getStatusManager().getStatus().toString()
		);
	}
	
	private Status getLastStatus(){
		String value = cache.getProperty(
				launch.getConfig().getId(), Property.STATUS.toString()
		);
		if(value != null){
			return Status.valueOf(value);
		}else{
			return null;
		}
	}
	
	private boolean isErrorsChanged() {
		
		long last = getErrorsHash();
		long current = computeErrorsHash();
		return (last == -1) || (last != current);
	}
	
	private void setErrorsHash(){
		cache.addProperty(
				launch.getConfig().getId(), Property.ERRORS.toString(), ""+computeErrorsHash()
		);
	}

	private long computeErrorsHash() {
		long hash = 0;
		for(Error error : launch.getNotifyingErrors()){
			hash += error.getHash();
		}
		return hash;
	}
	
	private long getErrorsHash(){
		String value = cache.getProperty(
				launch.getConfig().getId(), Property.ERRORS.toString()
		);
		if(value != null){
			return (new Long(value)).longValue();
		}else{
			return -1;
		}
	}

	private Mail createMail() {
		
		Mail mail = new Mail(Constants.APP_NAME+" [ "+launch.getConfig().getName()+" ]");
		mail.from = client.getConfig().getSmtpAddress();
		
		ArrayList<String> admins = getAdminAdresses();
		ArrayList<String> committers = getComitterAddresses();
		
		if(isNotifyAdmins()){
			mail.to = admins;
		}
		else if(isNotifyCommitters()){
			if(committers.size() > 0){
				mail.to = committers;
				mail.cc = admins;
			}else{
				mail.to = admins;
			}
		}
		
		mail.content = getMailContent();
		
		return mail;
	}

	private String getMailContent() {
		// TODO Auto-generated method stub
		return "";
	}
	
	private ArrayList<String> getAdminAdresses() {
		
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
	
	private ArrayList<String> getComitterAddresses() {
		
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
