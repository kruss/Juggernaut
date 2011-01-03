package smtp;

import html.AbstractHtmlPage;

import java.util.ArrayList;

import util.StringTools;

public class Mail {

	public String subject;
	public ArrayList<String> to;
	public ArrayList<String> cc;
	public String from;
	public String content;
	
	public Mail(String subject){
		
		this.subject = subject;
		this.to = new ArrayList<String>();
		this.cc = new ArrayList<String>();
		this.from = "";
		this.content = "";
	}
	
	public boolean isValid() {
		return !subject.isEmpty() && to.size()>0;
	}

	public String getHtml() {
		
		StringBuilder html = new StringBuilder();
		html.append("<html><head>\n");
		html.append(AbstractHtmlPage.getCSS());
		html.append("</head><body>\n");
		html.append("<hr>\n");
		html.append("<h1>"+subject+"</h1>\n");
		html.append("<ul>\n");
		html.append(" <li><b>To</b>: "+StringTools.join(to, "; ")+"</li>\n");
		html.append(" <li><b>Cc</b>: "+StringTools.join(cc, "; ")+"</li>\n");
		html.append("</ul>\n");
		html.append("<hr>\n");
		html.append(content);
		html.append("<hr>\n");
		html.append("</body></html>\n");
		return html.toString();
	}
}
