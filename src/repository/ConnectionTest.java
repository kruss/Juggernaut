package repository;

import launch.StatusManager.Status;
import logger.Logger;
import logger.Logger.Module;
import repository.IRepositoryClient.RevisionInfo;
import util.Task;
import util.UiTools;

public class ConnectionTest extends Task {

	public static final long TIMEOUT = 15 * 1000; // 15 sec
	
	private IRepositoryClient client;
	private String url;
	
	public ConnectionTest(IRepositoryClient client, String url, Logger logger) {
		
		super("ConnectionTest", logger);
		this.client = client;
		this.url = url;
		
	}

	@Override
	protected void runTask() {
		
		observer.log(Module.COMMON, "Test: "+url);
		Status status = Status.UNDEFINED;
		String message = "";
		try{
			RevisionInfo info = client.getInfo(url);
			status = Status.SUCCEED;
			message = "Url: "+url+"\nRevision: "+info.toString();
		}catch(Exception e){
			status = Status.FAILURE;
			message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
			if(!(e instanceof InterruptedException)){
				observer.error(Module.COMMON, e);
			}			
		}finally{
			observer.log(Module.COMMON, "Test: "+status.toString());
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
