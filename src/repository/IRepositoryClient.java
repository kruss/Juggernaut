package repository;

import java.util.ArrayList;
import java.util.Date;

public interface IRepositoryClient {

	public enum Revision { UNDEFINED, HEAD };
	
	/** get the current revision of uri */
	public String getRevision(String uri) throws Exception;
	
	/** get the time of a revision or null if not existent */
	public Date getRevisionTime(String Revision) throws Exception;
	
	/** checkout uri of given revision to destination-path */
	public void checkout(
			String uri, String revision, String destination
	) throws Exception;
	
	/** export file at uri of given revision */
	public String export(String uri, String revision) throws Exception;
	
	/** get all commits of uri within specified revisions */
	public ArrayList<RepositoryCommit> getCommits(
			String uri, String revision1, String revision2
	) throws Exception;
	
	public class RepositoryCommit {
		
		public String revision;
		public String author;
		public String message;
		public ArrayList<String> changes;
		
		public RepositoryCommit(){
			changes = new ArrayList<String>();
		}
	}
}
