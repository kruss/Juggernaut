package operation;

import java.util.ArrayList;

import repository.IRepositoryClient.CommitInfo;

public interface IRepositoryOperation {

	/** get the revision of the last run */
	public String getLastRevision();
	
	/** get the revision of the current run */
	public String getCurrentRevision();
	
	/** get all commits within interval: (last-revision, current-revision] */
	public ArrayList<CommitInfo> getCommits();
}
