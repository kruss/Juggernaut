package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;

import logger.Logger;
import logger.Logger.Mode;
import logger.Logger.Module;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import repository.SVNClient;
import repository.IRepositoryClient.CheckoutInfo;
import repository.IRepositoryClient.HistoryInfo;
import repository.IRepositoryClient.Revision;
import repository.IRepositoryClient.RevisionInfo;
import util.FileTools;
import util.SystemTools;

public class SVNClientTest {

	private static final String SVN_REPOSITORY = "svn://10.40.38.84:3690/cppdemo/trunk";
	
	private Logger logger;
	private SVNClient client;
	private File folder;
	
	public SVNClientTest(){
		logger = new Logger(Mode.CONSOLE);
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
		
		logger.info(Module.APP, "SVN Info");
		
		RevisionInfo result = null;
		try{
			result = client.getInfo(SVN_REPOSITORY);
		}catch(Exception e){
			logger.error(Module.APP, e);
			fail(e.getMessage());
		}
		
		assertTrue(result.revision != null);
		assertTrue(result.date != null);
		assertTrue(result.output != null);
	}

	@Test public void testCheckout() {
		
		logger.info(Module.APP, "SVN Checkout");
		assertTrue(folder.listFiles().length == 0);
		String revision = Revision.HEAD.toString();
		
		CheckoutInfo result = null;
		try{
			result = client.checkout(SVN_REPOSITORY, revision, folder.getAbsolutePath());
		}catch(Exception e){
			logger.error(Module.APP, e);
			fail(e.getMessage());
		}
		
		assertTrue(folder.listFiles().length > 0);
		assertTrue(result.revision != null);
		assertTrue(result.output != null);
	}
	
	@Test public void testHistory() {
		
		logger.info(Module.APP, "SVN History");
		
		HistoryInfo result = null;
		try{
			result = client.getHistory(SVN_REPOSITORY, "0", Revision.HEAD.toString());
		}catch(Exception e){
			logger.error(Module.APP, e);
			fail(e.getMessage());
		}
		
		assertTrue(result.revision1 != null);
		assertTrue(result.revision2 != null);
		assertTrue(result.commits.size() > 0);
		assertTrue(result.output != null);
	}
}
