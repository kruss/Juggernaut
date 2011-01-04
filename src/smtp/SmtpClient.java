package smtp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import core.ISystemComponent;
import logger.ILogger;
import logger.ILogConfig.Module;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** 
 * Performs email notifications via SMTP
 */
public class SmtpClient implements ISystemComponent, ISmtpClient {
	
	private ISmtpConfig config;
	
	@Override
	public ISmtpConfig getConfig(){ return config; }
	
	public SmtpClient(ISmtpConfig config){	
		this.config = config;
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	@Override
	public boolean isReady() {
		return config.isNotification() && !config.getSmtpServer().isEmpty();
	}
	
	@Override
	public void send(Mail mail, ILogger logger) throws Exception {
		
		if(isReady()){
			if(mail.isValid()){
				sendMail(mail, logger);
			}else{
				throw new Exception("Mail not valid!");
			}
		}else{
			throw new Exception("Client not ready!");
		}
	}

	private synchronized void sendMail(Mail mail, ILogger logger) throws Exception {
		
		logger.log(Module.SMTP, "Sending '"+mail.subject+"' to "+mail.to.size()+", cc "+mail.cc.size());
		
		Properties properties = new Properties(); 
		properties.put("mail.smtp.host", config.getSmtpServer());
		
		Session session = Session.getDefaultInstance(properties);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 
		session.setDebugOut(new PrintStream(buffer, true));
		session.setDebug(true);
		
		Message message = new MimeMessage(session);
		message.setFrom(createAddress(mail.from)); 
		message.setRecipients(RecipientType.TO, createAddresses(mail.to)); 
		message.setRecipients(RecipientType.CC, createAddresses(mail.cc)); 
		message.setSubject(mail.subject); 
		message.setContent(mail.content, "text/html");

		try{
			Transport.send(message);
			mail.setSend(true);
		}catch(Exception e){
			mail.setSend(false);
			throw e;
		}finally{
			String output = buffer.toString("UTF8").replaceAll("\\r\\n", "\n");
			logger.debug(Module.SMTP, "Output:\n"+output);
		}
	}

	private InternetAddress[] createAddresses(ArrayList<String> list) throws Exception  {
		
		InternetAddress[] address = new InternetAddress[list.size()];
		for(int i=0; i<list.size(); i++){
			address[i] = createAddress(list.get(i));
		}
		return address;
	}

	private InternetAddress createAddress(String item) throws Exception {
		return new InternetAddress(item);
	}
}
