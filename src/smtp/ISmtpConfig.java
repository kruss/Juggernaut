package smtp;

import java.util.ArrayList;

public interface ISmtpConfig {

	public boolean isNotification();
	
	public String getSmtpServer();
	
	public ArrayList<String> getAdministrators();
	
	public String getSmtpAddress();
}
