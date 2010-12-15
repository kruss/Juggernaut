package html;

public class HtmlLink {

	private String name;
	private String path;
	
	public HtmlLink(String name, String path) {
		this.name = name;
		this.path = path.replaceAll("\\\\", "/");
	}

	public String getHtml() {
		return "<a href='"+path+"'>"+name+"</a>";
	}
}
