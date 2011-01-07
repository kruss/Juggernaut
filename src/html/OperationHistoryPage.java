package html;

import data.OperationHistory;
import data.Error;

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
		html.append(getArtifactHtml());
		html.append(getErrorHtml());
		return html.toString();
	}
	
	protected String getErrorHtml(){
		
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
