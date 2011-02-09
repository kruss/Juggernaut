package core.launch.history;

import core.Result;
import core.html.HtmlLink;
import core.html.HtmlList;
import core.launch.data.Error;
import core.launch.data.ResultManager;

public class OperationHistoryPage extends AbstractHistoryPage {
	
	private OperationHistory history;
	
	public OperationHistoryPage(String name, String path, HtmlLink parent, OperationHistory history) {
		super(name, path, parent, history);
		this.history = history;
	}

	@Override
	public String getBody() {
		
		StringBuilder html = new StringBuilder();
		html.append(getGeneralHtml());
		html.append(getResultHtml());
		html.append(getArtifactHtml());
		html.append(getErrorHtml());
		return html.toString();
	}
	
	private String getResultHtml(){
		
		if(history.results.size() > 0){
			HtmlList list = new HtmlList("Results");
			for(Result result : history.results){
				list.addEntry(null, getResultHtml(result));
			}
			return list.getHtml();
		}else{
			return "";
		}
	}
	
	private String getResultHtml(Result result){
		
		StringBuilder html = new StringBuilder();
		if(result.status != Result.Status.UNDEFINED){
			html.append("<b>"+result.name+" - "+ResultManager.getStatusHtml(result.status)+"</b>\n");
		}else{
			html.append("<b>"+result.name+"</b>\n");
		}
		for(String key : result.properties.keySet()){
			String value = result.properties.get(key);
			html.append("<br>- "+key+": <i>"+value+"</i>\n");
		}
		if(!result.message.isEmpty()){
			html.append("<br>"+result.message.replaceAll("\\n", "<br>")+"\n");
		}
		if(result.results.size() > 0){
			HtmlList list = new HtmlList(null);
			for(Result child : result.results){
				list.addEntry(null, getResultHtml(child));
			}
			html.append(list.getHtml());
		}
		return html.toString();
	}
	
	private String getErrorHtml(){
		
		if(history.errors.size() > 0){
			HtmlList list = new HtmlList("Errors");
			list.setType(HtmlList.Type.OL);
			for(Error error : history.errors){
				list.addEntry(null, error.getHtml());
			}
			return list.getHtml();
		}else{
			return "";
		}
	}
}
