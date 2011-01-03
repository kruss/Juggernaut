package mail;

import logger.ILogger;
import logger.ILogConfig.Module;

public class SmtpClient implements IMailClient {

	private ISmtpConfig config;
	
	public SmtpClient(ISmtpConfig config){
		
		this.config = config;
	}
	
	@Override
	public void send(Mail mail, ILogger logger) throws Exception {
		
		logger.log(Module.SMTP, "Sending mail (to "+mail.to.size()+" / cc "+mail.cc.size()+") '"+mail.subject+"'");
		logger.debug(Module.SMTP, mail.content);
		// TODO
	}
}
