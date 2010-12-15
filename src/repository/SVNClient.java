package repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logger.Logger;
import logger.Logger.Module;

import util.CommandTask;
import util.SystemTools;

public class SVNClient implements IRepositoryClient {
	
	private Logger logger;
	
	public SVNClient(Logger logger){
		this.logger = logger;
	}
	
	@Override
	public RevisionInfo getInfo(String url) throws Exception {

		String name = "SVN Info";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "info "+url;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0, 0);
		if(!task.hasSucceded()){
			throw new Exception(name+" failed: "+task.getResult());
		}
		
		// get result
		RevisionInfo result = new RevisionInfo();
		result.output = task.getOutput();
		
		// find e.g: "Revision: 1234"
		Pattern p1 = Pattern.compile("^Revision: (\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m1 = p1.matcher(result.output);
		if(m1.find() && m1.groupCount() >= 1){
			result.revision = m1.group(1);
		}
		if(result.revision == null){
			throw new Exception(name+": no revision");
		}
		
		// find e.g: "Last Changed Date: 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010)"
		Pattern p2 = Pattern.compile("^Last Changed Date: (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
		Matcher m2 = p2.matcher(result.output);
		if(m2.find() && m2.groupCount() >= 1){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result.date = sdf.parse(m2.group(1));
		}
		if(result.date == null){
			throw new Exception(name+": no date");
		}
		
		logger.debug(Module.CMD, result.toString());
		return result;
	}

	@Override
	public CheckoutInfo checkout(String url, String revision, String destination) throws Exception {
		
		String name = "SVN Checkout";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "checkout -r "+revision+" "+url+" "+destination;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0, 0);
		if(!task.hasSucceded()){
			throw new Exception(name+" failed: "+task.getResult());
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
			throw new Exception(name+": no revision");
		}
		
		logger.debug(Module.CMD, result.toString());
		return result;
	}

	@Override
	public HistoryInfo getHistory(String url, String revision1, String revision2) throws Exception {

		String name = "SVN History";
		String path =  SystemTools.getWorkingDir();
		String command = "svn"; 
		String arguments = "log -r "+revision2+":"+revision1+" "+url;

		// perform task
		CommandTask task = new CommandTask(command, arguments, path, logger);
		task.syncRun(0, 0);
		if(!task.hasSucceded()){
			throw new Exception(name+" failed: "+task.getResult());
		}
		
		// get result
		HistoryInfo result = new HistoryInfo();
		result.revision1 = revision1;
		result.revision2 = revision2;
		result.commits = new ArrayList<CommitInfo>();
		result.output = task.getOutput();
		
		// find e.g: "r4 | kruss | 2010-11-26 12:47:16 +0100 (Fr, 26 Nov 2010) | 1 line"
		Pattern p = Pattern.compile("^r(\\d+) \\| (\\w+) \\| (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)", Pattern.MULTILINE | Pattern.UNIX_LINES);
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
			throw new Exception(name+": no commits");
		}

		logger.debug(Module.CMD, result.toString());
		return result;
	}
	
	@Override
	public String getNextRevision(String revision) throws Exception {

		if(revision.equals(Revision.HEAD.toString())){
			return Revision.HEAD.toString();
		}else{
			return ""+((new Integer(revision)).intValue()+1);
		}
	}
}
