package mail;

import core.ISystemComponent;
import logger.ILogger;
import logger.ILogConfig.Module;
import mail.ISmtpConfig.NotificationMode;
import mail.Mail.MailType;

/** 
 * Performs email notifications via SMTP
 */
public class SmtpManager implements ISystemComponent {
	
	private ISmtpConfig config;
	private IMailClient client;
	private ILogger logger;
	
	public ISmtpConfig getConfig(){ return config; }
	
	public SmtpManager(
			ISmtpConfig config, 
			IMailClient client, 
			ILogger logger)
	{	
		this.config = config;
		this.client = client;
		this.logger = logger;
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	public synchronized void send(Mail mail, ILogger logger) throws Exception {
		
		if(client != null && isSending(mail.type) && mail.to.size() > 0){
			
			this.logger.log(Module.SMTP, "Sending mail to "+mail.to.size()+" / cc "+mail.cc.size()+": '"+mail.subject+"'");
			try{
				client.send(mail, logger);
			}catch(Exception e){
				this.logger.error(Module.SMTP, "Unable to send mail: "+e.getMessage());
				throw e;
			}	
		}
	}

	private boolean isSending(MailType type) {

		if(config != null){
			NotificationMode mode = config.getNotificationMode();
			if(mode == NotificationMode.ADMINS){
				if(type == MailType.ADMIN){
					return true;
				}else{
					return false;
				}
			}else if(mode == NotificationMode.ADMINS_AND_COMMITTER){
				if(type == MailType.ADMIN || type == MailType.COMMITTER){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
}
