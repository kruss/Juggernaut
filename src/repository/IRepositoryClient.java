package repository;

import java.util.ArrayList;
import java.util.Date;

import util.StringTools;

public interface IRepositoryClient {

	public enum Revision { HEAD };
	
	/** get the current revision-info of uri */
	public RevisionInfo getInfo(String uri) throws Exception;
	
	/** checkout uri of given revision to destination-path,- returns the actual revision checked out */
	public String checkout(String uri, String revision, String destination) throws Exception;
	
	/** get history of uri within specified interval, where revision1 > revision2 */
	public ArrayList<RepositoryCommit> getHistory(String uri, String revision1, String revision2) throws Exception;
	
	public class RevisionInfo {
		
		public String revision;
		public Date date;
		
		public String toString(){
			return revision+" ("+StringTools.getTextDate(date)+")";
		}
	}
	
	public class RepositoryCommit {
		
		public String revision;
		public Date date;
		public String author;
		
		public String toString(){
			return author+" - "+revision+" ("+StringTools.getTextDate(date)+")";
		}
	}
}
