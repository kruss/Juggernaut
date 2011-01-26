package core.runtime.smtp;

import java.util.ArrayList;
import java.util.Date;

public class Mail {

	public String subject;
	public String from;
	public ArrayList<String> to;
	public ArrayList<String> cc;
	public String content;
	private Date send;
	
	public Mail(String subject){
		
		this.subject = subject;
		this.from = "";
		this.to = new ArrayList<String>();
		this.cc = new ArrayList<String>();
		this.content = "";
		send = null;
	}
	
	public void setSend(boolean send) {
		if(send){
			this.send = new Date();
		}else{
			this.send = null;
		}
	}
	public boolean isSend(){ return send != null; }
	
	public boolean isValid() {
		return !subject.isEmpty() && (to.size()>0 || cc.size()>0) && !from.isEmpty();
	}
}
