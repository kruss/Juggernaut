package smtp;

import logger.ILogger;

public interface ISmtpClient {

	public ISmtpConfig getConfig();
	public boolean isReady();
	public void send(Mail mail, ILogger logger) throws Exception;
}
