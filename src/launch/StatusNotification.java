package launch;

import java.util.ArrayList;

import smtp.ISmtpClient;

public class StatusNotification extends AbstractNotification {

	public StatusNotification(ISmtpClient client, LaunchAgent launch) {
		super(client, launch);
	}
	
	@Override
	protected String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - Status";
	}
	
	@Override
	protected ArrayList<String> getToAdresses() {
		return getAdministratorAdresses();
	}
	
	@Override
	protected ArrayList<String> getCcAdresses() {
		return new ArrayList<String>();
	}

	@Override
	protected String getContent() {
		return "";
	}
}
