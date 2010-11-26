package repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.CommandTask;
import util.Logger;
import util.SystemTools;

public class SVNClient implements IRepositoryClient {
	
	Logger logger;
	
	public SVNClient(Logger logger){
		this.logger = logger;
	}
	
	@Override
	public RevisionInfo getInfo(String uri) throws Exception {

		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "info "+uri;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0);
		if(!task.hasSucceded()){
			throw new Exception("SVN info failed: "+task.getResult());
		}
		
		RevisionInfo result = new RevisionInfo();
		String output = task.getOutput();
		// find e.g: "Revision: 1234"
		Pattern p1 = Pattern.compile("^Revision: (\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m1 = p1.matcher(output);
		if(m1.find() && m1.groupCount() >= 1){
			result.revision = m1.group(1);
		}
		// find e.g: "Last Changed Date: 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010)"
		Pattern p2 = Pattern.compile("^Last Changed Date: (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m2 = p2.matcher(output);
		if(m2.find() && m2.groupCount() >= 1){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result.date = sdf.parse(m2.group(1));
		}
		logger.debug("revision: "+result.toString());
		
		return result;
	}

	@Override
	public String checkout(String uri, String revision, String destination) throws Exception {
		
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "checkout -r "+revision+" "+uri+" "+destination;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0);
		if(!task.hasSucceded()){
			throw new Exception("SVN checkout failed: "+task.getResult());
		}
		
		String result = null;
		String output = task.getOutput();
		// find e.g: "Checked out revision 1234."
		Pattern p = Pattern.compile("^Checked out revision (\\d+).", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m = p.matcher(output);
		if(m.find() && m.groupCount() >= 1){
			result = m.group(1);
		}
		logger.debug("revision: "+result);
		
		return result;
	}

	@Override
	public ArrayList<RepositoryCommit> getHistory(String uri, String revision1, String revision2) throws Exception {

		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "log -r "+revision1+":"+revision2+" "+uri;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0);
		if(!task.hasSucceded()){
			throw new Exception("SVN history failed: "+task.getResult());
		}
		
		ArrayList<RepositoryCommit> result = new ArrayList<RepositoryCommit>();
		String output = task.getOutput();
		// find e.g: "r4 | kruss | 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010) | 1 line"
		Pattern p = Pattern.compile("^r(\\d+) \\| (\\w+) \\| (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m = p.matcher(output);
		while(m.find() && m.groupCount() >= 3){
			RepositoryCommit commit = new RepositoryCommit();
			commit.revision = m.group(1);
			commit.author = m.group(2);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			commit.date = sdf.parse(m.group(3));
			result.add(commit);
		}
		logger.debug("commits: "+result.size());
		for(RepositoryCommit commit : result){
			logger.debug("commit: "+commit.toString());
		}
		
		return result;
	}
}
