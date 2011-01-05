package launch;

import http.IHttpServer;

import java.util.ArrayList;

import core.History;

import smtp.ISmtpClient;

public class ErrorNotification extends AbstractNotification {

	public ErrorNotification(
			History history, ISmtpClient client, IHttpServer server, LaunchAgent launch
	) {
		super(history, client, server, launch);
	}
	
	@Override
	protected String getSubject() {
		return "Launch ["+launch.getConfig().getName()+"] - "+launch.getNotificationErrors().size()+" Error(s)";
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
		html.append(getUserHtml());
		html.append(getLinkHtml());
		return html.toString();
	}
}
