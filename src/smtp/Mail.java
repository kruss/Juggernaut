package smtp;

import html.AbstractHtmlPage;

import java.util.ArrayList;
import java.util.Date;

import util.DateTools;
import util.StringTools;

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
	
	public boolean isValid() {
		return !subject.isEmpty() && (to.size()>0 || cc.size()>0) && !from.isEmpty();
	}
	
	public void setSend(boolean send) {
		if(send){
			this.send = new Date();
		}else{
			this.send = null;
		}
	}
	public boolean isSend(){ return send != null; }

	public String getHtml() {
		
		StringBuilder html = new StringBuilder();
		html.append("<html><head>\n");
		html.append(AbstractHtmlPage.getCSS());
		html.append("</head><body>\n");
		html.append("<hr>\n");
		html.append("<h1>SMTP-Mail: '"+subject+"'</h1>\n");
		html.append("<ul>\n");
		html.append(" <li><b>From</b>: "+from+"</li>\n");
		html.append(" <li><b>To</b>: "+StringTools.join(to, "; ")+"</li>\n");
		html.append(" <li><b>Cc</b>: "+StringTools.join(cc, "; ")+"</li>\n");
		if(isSend()){
			html.append(" <li><b>Send</b>: "+DateTools.getTextDate(send)+"</li>\n");
		}else{
			html.append(" <li><b>Send</b>: -</li>\n");
		}
		html.append("</ul>\n");
		html.append("<hr>\n");
		html.append(content);
		html.append("<hr>\n");
		html.append("</body></html>\n");
		return html.toString();
	}
}
