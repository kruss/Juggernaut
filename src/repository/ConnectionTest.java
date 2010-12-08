package repository;

import launch.StatusManager.Status;
import repository.IRepositoryClient.RevisionInfo;
import core.Application;
import util.Task;
import util.UiTools;

public class ConnectionTest extends Task {

	public static final long TIMEOUT = 1 * 60 * 1000; // 1min
	
	private IRepositoryClient client;
	private String url;
	
	public ConnectionTest(IRepositoryClient client, String url) {
		
		super("ConnectionTest", Application.getInstance().getLogger());
		this.client = client;
		this.url = url;
		
	}

	@Override
	protected void runTask() {
		
		Status status = Status.UNDEFINED;
		String message = "";
		try{
			RevisionInfo info = client.getInfo(url);
			if(info.revision != null){
				status = Status.SUCCEED;
				message = "Url: "+url+"\nRevision: "+info.toString();
			}else{
				status = Status.ERROR;
				message = "Unable to retrieve Revision for:\n"+url;
			}
		}catch(Exception e){
			status = Status.FAILURE;
			observer.error(e);
			message = e.getMessage();
		}finally{
			if(status == Status.SUCCEED){
				UiTools.infoDialog(
						"Connection-Test ("+status.toString()+")\n\n"+message
				);
			}else{
				UiTools.errorDialog(
						"Connection-Test ("+status.toString()+")\n\n"+message
				);
			}
		}
	}

}
