package core.html;

import java.util.ArrayList;

public class HtmlTable {

	private String name;
	private String description;
	private ArrayList<HeaderCell> headers;
	private ArrayList<ContentCell> cells;
	
	public HtmlTable(String name){
		this.name = name;
		description = null;
		headers = new ArrayList<HeaderCell>();
		cells = new ArrayList<ContentCell>();
	}
	
	public void setDescription(String description){ this.description = description; }
	
	public void addHeaderCell(String name, int width){
		
		HeaderCell cell = new HeaderCell(name, width);
		headers.add(cell);
	}
	
	public void addContentCell(String content){
		
		ContentCell cell = new ContentCell(content);
		cells.add(cell);
	}
	
	public String getHtml(){
		
		StringBuilder html = new StringBuilder();
		if(name!=null){
			html.append("<h3>"+name+"</h3>\n");
		}
		if(description!=null){
			html.append("<p>"+description+"</p>\n");
		}
		html.append("<table cellspacing=0 cellpadding=3 border=1>\n");
		html.append("<tr>\n");
		for(HeaderCell cell : headers){
			html.append(cell.getHtml());
		}
		html.append("</tr>\n");
		int count = 0;
		for(ContentCell cell : cells){
			count++;
			if(count == 0){
				html.append("<tr>\n");
			}
			html.append(cell.getHtml());
			if(count > 0 && count % headers.size() == 0){
				html.append("</tr>\n");
				html.append("<tr>\n");
			}
		}
		html.append("</table>\n");
		return html.toString();
	}
	
	class HeaderCell {
		public String name;
		public int width;
		
		public HeaderCell(String name, int width){
			this.name = name;
			this.width = width;
		}
		
		public String getHtml(){
			return "<td width="+width+"><nobr><b>"+name+"</b></nobr></td>";
		}
	}
	
	class ContentCell {
		public String content;
		
		public ContentCell(String content){
			this.content = content;
		}
		
		public String getHtml(){
			return "<td><nobr>"+content+"</nobr></td>";
		}
	}
}
