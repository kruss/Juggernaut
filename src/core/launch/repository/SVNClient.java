package core.launch.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;



import util.CommandTask;
import util.SystemTools;

public class SVNClient implements IRepositoryClient {
	
	private TaskManager taskManager;
	private Logger logger;
	
	public SVNClient(TaskManager taskManager, Logger logger){
		this.taskManager = taskManager;
		this.logger = logger;
	}
	
	@Override
	public RevisionInfo getInfo(String url, long timeout) throws Exception {

		String name = "SVN Info";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "info "+url;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, taskManager, logger);
		task.syncRun(0, timeout);
		if(!task.hasSucceded()){
			throw new Exception(name+": failed ("+task.getResult()+") for "+url);
		}
		
		// get result
		RevisionInfo result = new RevisionInfo();
		result.output = task.getOutput();
		
		// find e.g: "Last Changed Rev: 1234"
		Pattern p1 = Pattern.compile("^Last Changed Rev: (\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m1 = p1.matcher(result.output);
		if(m1.find() && m1.groupCount() >= 1){
			result.revision = m1.group(1);
		}
		if(result.revision == null){
			throw new Exception(name+": no revision for "+url);
		}
		
		// find e.g: "Last Changed Date: 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010)"
		Pattern p2 = Pattern.compile("^Last Changed Date: (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m2 = p2.matcher(result.output);
		if(m2.find() && m2.groupCount() >= 1){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result.date = sdf.parse(m2.group(1));
		}
		if(result.date == null){
			throw new Exception(name+": no date for "+url);
		}
		
		logger.debug(Module.COMMAND, result.toString());
		return result;
	}

	@Override
	public CheckoutInfo checkout(String url, String revision, String destination, long timeout) throws Exception {
		
		String name = "SVN Checkout";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "checkout -r "+revision+" "+url+" "+destination;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, taskManager, logger);
		task.syncRun(0, timeout);
		if(!task.hasSucceded()){
			throw new Exception(name+": failed ("+task.getResult()+") for "+url+" ("+revision+")");
		}
		
		// get result
		CheckoutInfo result = new CheckoutInfo();
		result.output = task.getOutput();
		
		// find e.g: "Checked out revision 1234."
		Pattern p = Pattern.compile("^Checked out revision (\\d+).", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m = p.matcher(result.output);
		if(m.find() && m.groupCount() >= 1){
			result.revision = m.group(1);
		}
		if(result.revision == null){
			throw new Exception(name+": no revision for "+url+" ("+revision+")");
		}
		
		logger.debug(Module.COMMAND, result.toString());
		return result;
	}

	@Override
	public HistoryInfo getHistory(String url, String revision1, String revision2, long timeout) throws Exception {

		String name = "SVN History";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "log -r "+revision2+":"+revision1+" "+url;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, taskManager, logger);
		task.syncRun(0, timeout);
		if(!task.hasSucceded()){
			throw new Exception(name+" failed ("+task.getResult()+") for "+url+" ("+revision1+", "+revision2+")");
		}
		
		// get result
		HistoryInfo result = new HistoryInfo();
		result.revision1 = revision1;
		result.revision2 = revision2;
		result.commits = new ArrayList<CommitInfo>();
		result.output = task.getOutput();
		
		// find e.g: "r4 | kruss@lear.com | 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010) | 1 line"
		Pattern p = Pattern.compile("^r(\\d+) \\| (.+) \\| (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m = p.matcher(result.output);
		while(m.find() && m.groupCount() >= 3){
			CommitInfo commit = new CommitInfo();
			commit.revision = m.group(1);
			commit.author = m.group(2);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			commit.date = sdf.parse(m.group(3));
			result.commits.add(commit);
		}
		if(!revision1.equals(revision2) && result.commits.size() == 0){
			throw new Exception(name+": no commits for "+url+" ("+revision2+", "+revision1+")");
		}

		logger.debug(Module.COMMAND, result.toString());
		return result;
	}
	
	@Override
	public String getNextRevision(String revision, long timeout) throws Exception {

		if(revision.equals(Revision.HEAD.toString())){
			return Revision.HEAD.toString();
		}else{
			return ""+((new Integer(revision)).intValue()+1);
		}
	}
}
