package launch;

import java.util.ArrayList;
import java.util.Collections;

import operation.IRepositoryOperation;
import repository.IRepositoryClient.CommitInfo;
import repository.IRepositoryClient.HistoryInfo;

import mail.Mail;
import mail.SmtpManager;
import mail.ISmtpConfig.NotificationMode;
import mail.Mail.MailType;

/** performs the notification for a launch */
// TODO add checks to send only email when status change since last run or new errors
public class LaunchNotification {

	private LaunchAgent launch;
	private SmtpManager smtpManager;
	
	public LaunchNotification(LaunchAgent launch, SmtpManager smtpManager) {
		
		this.launch = launch;
		this.smtpManager = smtpManager;
	}

	public void performNotification() throws Exception {
		
		if(launch.getConfig().getNotificationMode() == NotificationMode.ADMINS){
			smtpManager.send(getAdminMail(launch), launch.getLogger());
		}
		
		if(launch.getConfig().getNotificationMode() == NotificationMode.ADMINS_AND_COMMITTER){
			smtpManager.send(getAdminMail(launch), launch.getLogger());
			smtpManager.send(getCommitterMail(launch), launch.getLogger());
		}
	}

	private Mail getAdminMail(LaunchAgent launch) {
		
		ArrayList<String> to = new ArrayList<String>();
		to.addAll(launch.getConfig().getAdministrators());
		
		ArrayList<String> cc = new ArrayList<String>();
		cc.addAll(smtpManager.getConfig().getAdministrators());
		
		Mail mail = new Mail(
				MailType.ADMIN, 
				"["+launch.getConfig().getName()+"] - Administrators", 
				smtpManager.getConfig().getSmtpAddress());
		mail.to = to;
		mail.cc = cc;
		mail.content = "Admin email..."; // TODO
		
		return mail;
	}
	
	private Mail getCommitterMail(LaunchAgent launch) {
		
		ArrayList<String> to = new ArrayList<String>();
		to.addAll(getComitters(launch));
		
		ArrayList<String> cc = new ArrayList<String>();
		cc.addAll(launch.getConfig().getAdministrators());
		cc.addAll(smtpManager.getConfig().getAdministrators());
		
		Mail mail = new Mail(
				MailType.ADMIN, 
				"["+launch.getConfig().getName()+"] - Committers", 
				smtpManager.getConfig().getSmtpAddress());
		mail.to = to;
		mail.cc = cc;
		mail.content = "Committer email..."; // TODO
		
		return mail;
	}
	
	
	private ArrayList<String> getComitters(LaunchAgent launch) {
		
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
