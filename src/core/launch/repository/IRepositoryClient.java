package core.launch.repository;

import java.util.ArrayList;
import java.util.Date;

import util.DateTools;

public interface IRepositoryClient {
	
	public enum Revision { HEAD };
	
	/** get the current revision-info of url */
	public RevisionInfo getInfo(String url, long timeout) throws Exception;
	
	public class RevisionInfo {
		
		public String revision;
		public Date date;
		public String output;
		
		public String toString(){
			return "Revision: "+revision+" ("+DateTools.getTextDate(date)+")";
		}
	}

	/** checkout url of given revision to destination-path and returns the checkout-info */
	public CheckoutInfo checkout(String url, String revision, String destination, long timeout) throws Exception;
	
	public class CheckoutInfo {
		
		public String revision;
		public String output;
		
		public String toString(){
			return "Checkout: "+revision;
		}
	}
	
	/** get history of url within interval: [revision1, revision2] */
	public HistoryInfo getHistory(String url, String revision1, String revision2, long timeout) throws Exception;

	public class HistoryInfo {
		
		public String revision1;
		public String revision2;
		public ArrayList<CommitInfo> commits;
		public String output;
		
		public String toString(){
			StringBuilder text = new StringBuilder();
			text.append("History ["+revision1+" - "+revision2+"]\n");
			for(CommitInfo commit : commits){
				text.append("- "+commit.toString()+"\n");
			}
			return text.toString();
		}
	}
	
	public class CommitInfo {
		
		public String revision;
		public Date date;
		public String author;
		
		public String toString(){
			return "Revision "+revision+" - "+author+" ("+DateTools.getTextDate(date)+")";
		}
	}
	
	/** get the next revision to the specified one */
	public String getNextRevision(String revision, long timeout) throws Exception;
}
