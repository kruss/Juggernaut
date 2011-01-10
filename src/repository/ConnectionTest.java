package repository;

import core.TaskManager;
import launch.StatusManager.Status;

import logger.Logger;
import logger.ILogConfig.Module;
import repository.IRepositoryClient.RevisionInfo;
import util.Task;
import util.UiTools;

public class ConnectionTest extends Task {

	public static final long TIMEOUT = 15 * 1000; // 15 sec
	
	private IRepositoryClient client;
	private String url;
	private Logger logger;
	
	public ConnectionTest(IRepositoryClient client, String url, TaskManager taskManager, Logger logger) {
		
		super("ConnectionTest", taskManager);
		this.client = client;
		this.url = url;
		this.logger = logger;
	}

	@Override
	protected void runTask() {
		
		logger.log(Module.COMMON, "Test: "+url);
		Status status = Status.UNDEFINED;
		String message = "";
		try{
			RevisionInfo info = client.getInfo(url);
			status = Status.SUCCEED;
			message = url+"\n"+info.toString();
		}catch(Exception e){
			status = Status.FAILURE;
			message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
			if(!(e instanceof InterruptedException)){
				logger.error(Module.COMMON, e);
			}			
		}finally{
			logger.log(Module.COMMON, "Test: "+status.toString());
			if(status == Status.SUCCEED){
				UiTools.infoDialog(
						"Test - "+status.toString()+"\n\n"+message
				);
			}else{
				UiTools.errorDialog(
						"Test - "+status.toString()+"\n\n"+message
				);
			}
		}
	}

}
