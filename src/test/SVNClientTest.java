package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import repository.SVNClient;
import repository.IRepositoryClient.RepositoryCommit;
import repository.IRepositoryClient.Revision;
import repository.IRepositoryClient.RevisionInfo;
import util.FileTools;
import util.Logger;
import util.SystemTools;
import util.Logger.Mode;

public class SVNClientTest {

	private static final String SVN_REPOSITORY = "svn://10.40.38.84:3690/cppdemo/trunk";
	
	private Logger logger;
	private SVNClient client;
	private File folder;
	
	public SVNClientTest(){
		Logger.VERBOSE = true;
		logger = new Logger(Mode.CONSOLE_ONLY);
		client = new SVNClient(logger);
		folder = new File(SystemTools.getWorkingDir()+File.separator+(new Date()).getTime()); 
	}
	
	@Before  
	public void runBeforeEveryTest() {   
	    try{
	    	FileTools.createFolder(folder.getAbsolutePath());
		}catch(Exception e){
			fail(e.getMessage());
		}
	}   
	  
	@After  
	public void runAfterEveryTest() {   
	    try{
			FileTools.deleteFolder(folder.getAbsolutePath());
		}catch(Exception e){
			fail(e.getMessage());
		}  
	} 
	
	@Test public void testInfo() {
		
		logger.info("SVN Info");
		
		RevisionInfo result = null;
		try{
			result = client.getInfo(SVN_REPOSITORY);
		}catch(Exception e){
			logger.error(e);
			fail(e.getMessage());
		}
		
		assertTrue(result.revision != null);
		assertTrue(result.date != null);
	}

	@Test public void testCheckout() {
		
		logger.info("SVN Checkout");
		assertTrue(folder.listFiles().length == 0);
		String revision = Revision.HEAD.toString();
		String result = null;
		try{
			result = client.checkout(SVN_REPOSITORY, revision, folder.getAbsolutePath());
		}catch(Exception e){
			logger.error(e);
			fail(e.getMessage());
		}
		assertTrue(folder.listFiles().length > 0);
		assertTrue(result != null);
	}
	
	@Test public void testHistory() {
		
		logger.info("SVN History");
		
		ArrayList<RepositoryCommit> result = null;
		try{
			result = client.getHistory(SVN_REPOSITORY, Revision.HEAD.toString(), "0");
		}catch(Exception e){
			logger.error(e);
			fail(e.getMessage());
		}
		
		assertTrue(result.size() > 0);
	}
}
