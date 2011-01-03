package smtp;

import smtp.ISmtpConfig.NotificationMode;
import core.ISystemComponent;
import logger.ILogger;
import logger.ILogConfig.Module;

/** 
 * Performs email notifications via SMTP
 */
public class SmtpClient implements ISystemComponent {
	
	private ISmtpConfig config;
	
	public ISmtpConfig getConfig(){ return config; }
	
	public SmtpClient(ISmtpConfig config){	
		this.config = config;
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	public synchronized void send(Mail mail, ILogger logger) throws Exception {
		
		if(isReady() && mail.isValid()){
			logger.log(Module.SMTP, "Sending mail to "+mail.to.size()+" / cc "+mail.cc.size()+": '"+mail.subject+"'");
			// TODO
		}else{
			throw new Exception("Illegal state!");
		}
	}

	public boolean isReady() {
		return 
			!config.getSmtpServer().isEmpty() && config.getNotificationMode() != NotificationMode.DISABLED;
	}
}
