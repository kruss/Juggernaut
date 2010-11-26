package repository;

import java.util.ArrayList;
import java.util.Date;

import util.CommandTask;
import util.Logger;
import util.SystemTools;

public class SVNClient implements IRepositoryClient {
	
	Logger logger;
	
	public SVNClient(Logger logger){
		this.logger = logger;
	}
	
	@Override
	public String getRevision(String uri) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Date getRevisionTime(String Revision) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkout(String uri, String revision, String destination) throws Exception {
		
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		ArrayList<String> arguments = new ArrayList<String>();
		arguments.add("checkout -r "+revision);
		arguments.add(uri);
		arguments.add(destination);

		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0);
		if(!task.hasSucceded()){
			throw new Exception("SVN Checkout failed: "+task.getReturnValue());
		}
	}

	@Override
	public String export(String uri, String revision) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<RepositoryCommit> getCommits(String uri, String revision1,
			String revision2) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
