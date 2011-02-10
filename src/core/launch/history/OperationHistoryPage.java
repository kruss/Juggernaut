package core.launch.history;

import core.Result;
import core.html.HtmlLink;
import core.html.HtmlList;
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
				list.addEntry(null, ResultManager.getResultHtml(result));
			}
			return list.getHtml();
		}else{
			return "";
		}
	}
}
