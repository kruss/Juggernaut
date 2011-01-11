package smtp;

import core.Constants;
import logger.Logger;
import ui.AbstractUITest;
import util.UiTools;

public class SmtpTest extends AbstractUITest {

	private ISmtpClient client;
	
	public SmtpTest(ISmtpClient client, Logger logger) {
		super(logger);
		this.client = client;
	}

	@Override
	protected String performTest(String content) throws Exception {
		
		if(client.isReady()){
			String address = UiTools.inputDialog("Send Test-Mail:", "");
			if(address != null && !address.isEmpty()){
				Mail mail = new Mail("Test");
				mail.from = client.getConfig().getSmtpAddress();
				mail.to.add(address);
				mail.content = "Send by "+Constants.APP_NAME+"!";
				client.send(mail, logger);
				return "Mail send to: "+address;
			}else{
				throw CANCEL;
			}
		}else{
			throw new Exception("Client not ready");
		}
	}
}
