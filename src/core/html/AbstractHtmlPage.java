package core.html;

import java.util.ArrayList;
import java.util.Date;

import core.Constants;

import util.DateTools;
import util.FileTools;

public abstract class AbstractHtmlPage {

	private static final int REFRESH = 5 * 60; // sec
	
	private String name;
	private String path;
	private HtmlLink parent;
	private ArrayList<ChildPage> childs;
	
	public void addChild(AbstractHtmlPage page, HtmlLink link){
		childs.add(new ChildPage(page, link));
	}
	
	protected boolean expires;	// if set the page will always be loaded from original location
	protected boolean refresh;	// if set the page will auto-reload in given interval
	
	public AbstractHtmlPage(String name, String path, HtmlLink parent){
		
		this.name = name;
		this.path = path;
		this.parent = parent;
		childs = new ArrayList<ChildPage>();
		
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
			html.append("<meta http-equiv=\"refresh\" content=\""+REFRESH+"\">\n");
		}
		html.append(getCSS());
		html.append("</head><body>\n");
		if(parent != null){
			html.append("<hr><h1>"+parent.getHtml()+" "+name+"</h1><hr>\n");
		}else{
			html.append("<hr><h1>"+name+"</h1><hr>\n");
		}
		if(childs.size() > 0){
			for(int i=0; i<childs.size(); i++){
				html.append(childs.get(i).link.getHtml()+(i < childs.size()-1 ? " / " : ""));
			}
			html.append("<hr>");
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
		
		if(path != null){
			String html = getHtml();
			FileTools.writeFile(path, html, false);
			
			for(ChildPage child : childs){
				child.page.create();
			}
		}
	}
	
	private class ChildPage {
		
		public AbstractHtmlPage page;
		public HtmlLink link;
		
		public ChildPage(AbstractHtmlPage page, HtmlLink link){
			
			this.page = page;
			this.link = link;
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
