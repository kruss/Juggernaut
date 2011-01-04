package smtp;

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
	
	public boolean isReady() {
		return config.isNotification() && !config.getSmtpServer().isEmpty();
	}
	
	public synchronized void send(Mail mail, ILogger logger) throws Exception {
		
		if(isReady()){
			if(mail.isValid()){
				if(sendMail(mail, logger)){
					logger.debug(Module.SMTP, "Sending (to "+mail.to.size()+", cc "+mail.cc.size()+"): "+mail.subject);
					mail.setSend(true);
				}else{
					logger.debug(Module.SMTP, "Unable to send (to "+mail.to.size()+", cc "+mail.cc.size()+"): "+mail.subject);
					mail.setSend(false);
				}
			}else{
				throw new Exception("Mail not valid!");
			}
		}else{
			throw new Exception("Client not ready!");
		}
	}

	private boolean sendMail(Mail mail, ILogger logger) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
