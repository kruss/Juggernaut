package mail;

import logger.ILogger;

public interface IMailClient {
	
	public void send(Mail mail, ILogger logger) throws Exception ;
}
