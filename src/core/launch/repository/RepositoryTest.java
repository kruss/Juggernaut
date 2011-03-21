package core.launch.repository;

import core.launch.data.StatusManager.Status;
import core.launch.repository.IRepositoryClient.RevisionInfo;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import ui.dialog.AbstractUITest;
import util.Task;

public class RepositoryTest extends AbstractUITest {
	
	public static final int TIMEOUT = 5 * 1000; // 5 sec

	private TaskManager taskManager;
	private IRepositoryClient client;
	
	public RepositoryTest(TaskManager taskManager, IRepositoryClient client, Logger logger){
		super(logger);
		
		this.taskManager = taskManager;
		this.client = client;
	}

	@Override
	protected TestStatus performTest(String url) throws Exception {
		
		if(!url.isEmpty()){			
			TestTask task = new TestTask(getTestName(), taskManager, url);
			task.syncRun(0, TIMEOUT);
			return task.result;
		}else{
			return new TestStatus(Status.ERROR, "Missing URL");
		}
	}
	
	private class TestTask extends Task {
		
		private String url;
		public TestStatus result;
		
		public TestTask(String name, TaskManager taskManager, String url) {
			super(name, taskManager);
			
			this.url = url;
			result = new TestStatus(Status.UNDEFINED, "invalid");
		}

		@Override
		protected void runTask() {
			try{
				RevisionInfo info = client.getInfo(url);
				result = new TestStatus(Status.SUCCEED, info.toString());
			}catch(Exception e){
				result = new TestStatus(e);
				logger.error(Module.COMMON, e);
			}
		}		
	}
}
