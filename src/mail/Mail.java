package mail;

import java.util.ArrayList;

public class Mail {

	public enum MailType { ADMIN, COMMITTER }
	
	public MailType type;
	public String subject;
	public ArrayList<String> to;
	public ArrayList<String> cc;
	public String from;
	public String content;
	
	public Mail(MailType type, String subject, String from){
		
		this.type = type;
		this.subject = subject;
		this.to = new ArrayList<String>();
		this.cc = new ArrayList<String>();
		this.from = from;
		this.content = "";
	}
}
