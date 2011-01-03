package mail;

import java.util.ArrayList;

public interface ISmtpConfig {

	public enum NotificationMode { DISABLED, ADMINS, ADMINS_AND_COMMITTER }

	public NotificationMode getNotificationMode();
	
	public String getSmtpServer();
	
	public ArrayList<String> getAdministrators();
	
	public String getSmtpAddress();
}
