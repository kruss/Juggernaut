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
import repository.IRepositoryClient.Revision;
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
		
		logger.info(Module.COMMON, "SVN Info");
		
		try{
			client.getInfo(SVN_REPOSITORY);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}

	@Test public void testCheckout() {
		
		logger.info(Module.COMMON, "SVN Checkout");
		assertTrue(folder.listFiles().length == 0);
		String revision = Revision.HEAD.toString();
		
		try{
			client.checkout(SVN_REPOSITORY, revision, folder.getAbsolutePath());
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
		assertTrue(folder.listFiles().length > 0);
	}
	
	@Test public void testHistory() {
		
		logger.info(Module.COMMON, "SVN History");
		
		try{
			client.getHistory(SVN_REPOSITORY, "0", Revision.HEAD.toString());
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
	
	@Test public void getNextRevision() {
		
		try{
			assertTrue(client.getNextRevision(Revision.HEAD.toString()).equals(Revision.HEAD.toString()));
			assertTrue(client.getNextRevision("0").equals("1"));
		}catch(Exception e) {
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
}
