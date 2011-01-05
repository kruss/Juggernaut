package html;

import data.OperationHistory;

public class OperationHistoryPage extends AbstractHistoryPage {
	
	@SuppressWarnings("unused")
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
}
