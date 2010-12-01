package repository;

import java.util.ArrayList;
import java.util.Date;

import util.StringTools;

public interface IRepositoryClient {

	public enum Revision { HEAD };
	
	/** get the current revision-info of url */
	public RevisionInfo getInfo(String url) throws Exception;
	
	public class RevisionInfo {
		
		public String revision;
		public Date date;
		
		public String toString(){
			if(revision != null && date != null){
				return revision+" ("+StringTools.getTextDate(date)+")";
			}else{
				return "<invalid>";
			}
		}
	}

	/** checkout url of given revision to destination-path and returns the checkout-info */
	public CheckoutInfo checkout(String url, String revision, String destination) throws Exception;
	
	public class CheckoutInfo {
		
		public String revision;
		public String output;
		
		public String toString(){
			if(revision != null){
				return revision;
			}else{
				return "<invalid>";
			}
		}
	}
	
	/** get history of url within specified interval, where revision1 > revision2 */
	public ArrayList<RepositoryCommit> getHistory(String url, String revision1, String revision2) throws Exception;

	public class RepositoryCommit {
		
		public String revision;
		public Date date;
		public String author;
		
		public String toString(){
			if(revision != null && date != null && author != null){
				return author+" - "+revision+" ("+StringTools.getTextDate(date)+")";
			}else{
				return "<invalid>";
			}
		}
	}
}
