package core.runtime.smtp;

import core.runtime.logger.ILogger;

public interface ISmtpClient {

	public ISmtpConfig getConfig();
	public boolean isReady();
	public void send(Mail mail, ILogger logger) throws Exception;
}
