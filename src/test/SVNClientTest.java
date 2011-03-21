package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;




import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.launch.repository.SVNClient;
import core.launch.repository.IRepositoryClient.Revision;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.logger.Logger.Mode;

import util.FileTools;
import util.SystemTools;

public class SVNClientTest {

	private static final String SVN_REPOSITORY = "svn://10.40.38.84:3690/cppdemo/trunk";
	public static final int SVN_TIMEOUT = 30 * 1000; // 30 sec
	
	private Logger logger;
	private TaskManager taskManager;
	private SVNClient client;
	private File folder;
	
	public SVNClientTest(){
		logger = new Logger(null, Mode.CONSOLE);
		taskManager = new TaskManager(logger);
		client = new SVNClient(taskManager, logger);
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
			client.getInfo(SVN_REPOSITORY, SVN_TIMEOUT);
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
			client.checkout(SVN_REPOSITORY, revision, folder.getAbsolutePath(), SVN_TIMEOUT);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
		assertTrue(folder.listFiles().length > 0);
	}
	
	@Test public void testHistory() {
		
		logger.info(Module.COMMON, "SVN History");
		
		try{
			client.getHistory(SVN_REPOSITORY, "0", Revision.HEAD.toString(), SVN_TIMEOUT);
		}catch(Exception e){
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
	
	@Test public void getNextRevision() {
		
		try{
			assertTrue(client.getNextRevision(Revision.HEAD.toString(), SVN_TIMEOUT).equals(Revision.HEAD.toString()));
			assertTrue(client.getNextRevision("0", SVN_TIMEOUT).equals("1"));
		}catch(Exception e) {
			logger.error(Module.COMMON, e);
			fail(e.getMessage());
		}
	}
}
