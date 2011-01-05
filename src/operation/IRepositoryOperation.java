package operation;

import repository.IRepositoryClient.HistoryInfo;

public interface IRepositoryOperation {

	/** get the url of the current run */
	public String getUrl();
	
	/** get the revision of the current run */
	public String getRevision();
	
	/** get the history since last run */
	public HistoryInfo getHistory();
}
