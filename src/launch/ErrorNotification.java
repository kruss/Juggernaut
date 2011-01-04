package launch;

import http.IHttpServer;

import java.util.ArrayList;

import smtp.ISmtpClient;

public class ErrorNotification extends AbstractNotification {

	public ErrorNotification(
			ISmtpClient client, IHttpServer server, LaunchAgent launch
	) {
		super(client, server, launch);
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

		StringBuilder html = new StringBuilder();
		html.append("<h1>Launch ["+launch.getConfig().getName()+"]</h1>\n");
		// TODO show errors / legacy errors / commits
		html.append(getUserInfo());
		html.append(getLinkInfo());
		return html.toString();
	}
}
