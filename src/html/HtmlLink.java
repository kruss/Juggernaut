package html;

public class HtmlLink {

	private String name;
	private String path;
	private boolean extern;
	
	public HtmlLink(String name, String path) {
		this.name = name;
		this.path = path.replaceAll("\\\\", "/");
		extern = false;
	}
	
	public void setExtern(boolean extern) {
		this.extern = extern;
	}

	public String getHtml() {
		
		if(extern){
			return "<a target='_blank' href='"+path+"'>"+name+"</a>";
		}else{			
			return "<a href='"+path+"'>"+name+"</a>";
		}
	}
}
