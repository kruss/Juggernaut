package core.runtime.smtp;

import java.util.ArrayList;

public interface ISmtpConfig {

	public boolean isNotification();
	public String getSmtpServer();
	public String getHostAddress();
	public ArrayList<String> getAdministratorAddresses();
}
