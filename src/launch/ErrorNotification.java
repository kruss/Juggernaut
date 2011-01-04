package launch;

import java.util.ArrayList;

import smtp.ISmtpClient;

public class ErrorNotification extends AbstractNotification {

	public ErrorNotification(ISmtpClient client, LaunchAgent launch) {
		super(client, launch);
	}
	
	@Override
	protected String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - Error(s)";
	}
	
	@Override
	protected ArrayList<String> getToAdresses() {
		return getComitterAddresses();
	}
	
	@Override
	protected ArrayList<String> getCcAdresses() {
		return getAdministratorAdresses();
	}

	@Override
	protected String getContent() {
		return "";
	}
}
