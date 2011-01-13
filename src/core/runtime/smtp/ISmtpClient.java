package core.runtime.smtp;

import core.runtime.logger.Logger;

public interface ISmtpClient {

	public ISmtpConfig getConfig();
	public boolean isReady();
	public void send(Mail mail, Logger logger) throws Exception;
}
