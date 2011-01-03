package smtp;

import java.util.ArrayList;

public interface ISmtpConfig {

	public enum NotificationMode { DISABLED, ADMINSTRATORS, COMMITTER }

	public NotificationMode getNotificationMode();
	
	public String getSmtpServer();
	
	public ArrayList<String> getAdministrators();
	
	public String getSmtpAddress();
}
