package core.runtime.smtp;

import core.Constants;
import core.launch.data.StatusManager.Status;
import core.runtime.logger.Logger;
import ui.dialog.AbstractUITest;
import util.UiTools;

public class SmtpTest extends AbstractUITest {

	private ISmtpClient client;
	
	public SmtpTest(ISmtpClient client, Logger logger) {
		super(logger);
		this.client = client;
	}

	@Override
	protected TestStatus performTest(String server) throws Exception {
		
		if(client.isReady()){
			
			String address = UiTools.inputDialog("Send Test-Mail:", "");
			if(address != null && !address.isEmpty()){
				
				Mail mail = new Mail("Test");
				mail.from = client.getConfig().getSmtpAddress();
				mail.to.add(address);
				mail.content = "Send by "+Constants.APP_NAME+"!";
				
				client.send(mail, logger);
				return new TestStatus(Status.SUCCEED, "Mail send to: "+address);
			}else{
				return new TestStatus(Status.CANCEL, "cancelded");
			}
		}else{
			return new TestStatus(Status.ERROR, "Client not ready");
		}
	}
}
