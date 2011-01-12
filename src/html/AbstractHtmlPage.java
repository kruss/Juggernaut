package html;

import java.util.ArrayList;
import java.util.Date;

import core.Constants;

import util.DateTools;
import util.FileTools;

public abstract class AbstractHtmlPage {

	protected String name;
	protected String path;
	protected HtmlLink parent;
	protected ArrayList<AbstractHtmlPage> childs;
	
	public String getName(){ return name; }
	public String getPath(){ return path; }
	
	protected boolean expires;
	protected boolean refresh;
	
	public AbstractHtmlPage(String name, String path, HtmlLink parent){
		
		this.name = name;
		this.path = path;
		this.parent = parent;
		childs = new ArrayList<AbstractHtmlPage>();
		
		expires = false;
		refresh = false;
	}
	
	public String getHtml(){
		
		StringBuilder html = new StringBuilder();
		html.append(getHeader());
		html.append(getBody());
		html.append(getFooter());
		return html.toString();
	}

	public String getHeader(){
		
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>"+name+"</title>\n");
		if(expires){
			html.append("<meta http-equiv=\"expires\" content=\"0\">\n");
		}
		if(refresh){
			html.append("<meta http-equiv=\"refresh\" content=\"60\">\n");
		}
		html.append(getCSS());
		html.append("</head><body>\n");
		if(parent != null){
			html.append("<hr><h1>"+parent.getHtml()+" "+name+"</h1><hr>\n");
		}else{
			html.append("<hr><h1>"+name+"</h1><hr>\n");
		}
		if(childs.size() > 0){
			html.append("<p class='small'>&gt;&gt; ");
			for(AbstractHtmlPage child : childs){
				HtmlLink link = new HtmlLink(child.getName(), child.getPath());
				html.append("["+link.getHtml()+"] ");
			}
			html.append("</p>");
		}
		html.append("<p>");
		return html.toString();
	}

	private String getFooter() {
		
		StringBuilder html = new StringBuilder();
		html.append("</p>");
		html.append("<hr><i class='small'>\n");
		html.append(Constants.APP_FULL_NAME+" - "+DateTools.getTextDate((new Date())));
		html.append("</i><hr></body>\n");
		return html.toString();
	}
	
	public abstract String getBody();
	
	public void create() throws Exception {
		
		String html = getHtml();
		FileTools.writeFile(path, html, false);
		
		for(AbstractHtmlPage child : childs){
			child.create();
		}
	}
	
	public static String getCSS(){
		
		StringBuilder css = new StringBuilder();
		css.append("<style type=\"text/css\"> \n");
		css.append("  h1				{ font-family:'Arial,sans-serif'; font-size:14pt; font-weight:bold; } \n");
		css.append("  h2				{ font-family:'Arial,sans-serif'; font-size:12pt; font-weight:bold; } \n");
		css.append("  h3				{ font-family:'Arial,sans-serif'; font-size:11pt; font-weight:bold; } \n");
		css.append("  h4				{ font-family:'Arial,sans-serif'; font-size:10pt; font-weight:bold; } \n");
		css.append("  body				{ font-family:'Arial,sans-serif'; font-size:8pt; } \n");
		css.append("  p,b,u,i,td,li		{ font-family:'Arial,sans-serif'; font-size:8pt; } \n");
		css.append("  a:link 			{ color:blue; text-decoration:none; } \n");
		css.append("  a:visited 		{ color:blue; text-decoration:none; } \n");
		css.append("  a:focus 			{ color:orange; text-decoration:none; } \n");
		css.append("  a:hover 			{ color:orange; text-decoration:none; } \n");
		css.append("  a:active 			{ color:orange; text-decoration:none; } \n");
		css.append("  .small			{ font-family:'Arial,sans-serif'; font-size:7pt; } \n");
		css.append("  .tiny				{ font-family:'Arial,sans-serif'; font-size:6pt; } \n");
		css.append("</style> \n");
		return css.toString();
	}
}
