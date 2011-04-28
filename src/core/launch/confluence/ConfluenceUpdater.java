package core.launch.confluence;

import util.DateTools;
import util.StringTools;
import util.SystemTools;
import core.Constants;
import core.launch.LaunchAgent;
import core.launch.data.StatusManager.Status;
import core.launch.history.LaunchHistory;
import core.launch.history.OperationHistory;
import core.launch.operation.AbstractOperation;
import core.persistence.History;
import core.runtime.confluence.IConfluenceClient;
import core.runtime.confluence.IConfluenceConfig;
import core.runtime.confluence.IConfluenceClient.PageInfo;
import core.runtime.http.IHttpConfig;
import core.runtime.logger.ILogConfig.Module;

public class ConfluenceUpdater {

	private static final int HISTORY_MAX = 5;
	private static final int DESCRIPTION_MAX = 150;
	
	private IConfluenceConfig confluenceConfig;
	private IHttpConfig httpConfig;
	private IConfluenceClient client;
	private LaunchAgent launch;
	private LaunchHistory previous;
	
	public ConfluenceUpdater(
			History history, IHttpConfig httpConfig, 
			IConfluenceConfig confluenceConfig, IConfluenceClient client, 
			LaunchAgent launch)
	{
		this.confluenceConfig = confluenceConfig;
		this.client = client;
		this.httpConfig = httpConfig;
		this.launch = launch;
		this.previous = history.getLatest(launch.getConfig().getId());
	}

	public boolean isReady() {

		return 
			!confluenceConfig.getConfluenceServer().isEmpty() &&
			launch.getConfig().isConfluenceUpdate() && !launch.getConfig().getConfluenceSpace().isEmpty() &&
			(
					launch.getStatusManager().getStatus() == Status.SUCCEED ||
					launch.getStatusManager().getStatus() == Status.ERROR
			);
	}

	public void update() throws Exception {
		
		client.login(confluenceConfig.getConfluenceServer(), IConfluenceClient.RPC_USER, IConfluenceClient.RPC_USER, launch.getLogger());
		try{
			updateLaunchPage();
			updateLinkPage();
		}finally{
			client.logout(launch.getLogger());
		}
	}
	
	private void updateLaunchPage() throws Exception {
		
		String pageTitle = getLaunchPageTitle();
		String pageContent = getLaunchPageContent();
		PageInfo pageInfo = client.getInfo(launch.getConfig().getConfluenceSpace(), pageTitle, launch.getLogger());
		client.updatePage(pageInfo, pageContent, launch.getLogger());
	}
	
	private void updateLinkPage() throws Exception {
		
		String pageTitle = getLinkPageTitle();
		String pageContent = getLinkPageContent();
		PageInfo pageInfo = client.getInfo(launch.getConfig().getConfluenceSpace(), pageTitle, launch.getLogger());
		client.updatePage(pageInfo, pageContent, launch.getLogger());
	}
	
	private String getLaunchPageTitle() {
		return  Constants.APP_NAME+"_"+launch.getConfig().getName();
	}
	
	private String getLinkPageTitle() {
		return  Constants.APP_NAME+"_"+launch.getConfig().getName()+"_Link";
	}

	private String getLaunchPageContent() throws Exception {
		
		StringBuilder content = new StringBuilder();
		content.append(getGeneralContent());
		content.append(getOperationsContent());
		return content.toString();
	}
	
	private String getGeneralContent() {

		StringBuilder content = new StringBuilder();
		content.append(
				"h2. Launch \\[ "+launch.getConfig().getName()+" \\] - "+
				DateTools.getTextDate(launch.getStatusManager().getStart())+"\r\n\r\n"
		);
		Status currentStatus = launch.getStatusManager().getStatus();
		Status lastStatus = previous != null ? previous.status : null;
		if(lastStatus != null && lastStatus != currentStatus){
			content.append("* Status: "+getStatusIcon(lastStatus)+" -> "+getStatusIcon(currentStatus)+"\r\n");
		}else{
			content.append("* Status: "+getStatusIcon(currentStatus)+"\r\n");
		}
		String description = launch.getConfig().getDescription();
		if(!description.isEmpty()){
			content.append("* Description: "+description+"\r\n");
		}
		String trigger = launch.getTrigger().getStatus().message;
		if(!trigger.isEmpty()){
			content.append("* Trigger: "+trigger+"\r\n");
		}
		if(httpConfig.isHttpServer()){
			try{
				String url = 
					"http://"+SystemTools.getHostName()+":"+httpConfig.getHttpPort()+
					"/"+launch.getStatusManager().getStart().getTime()+"/"+Constants.INDEX_NAME+".htm";
				content.append("* Logfile: ["+url+"|"+url+"]\r\n");
			}catch(Exception e){
				launch.getLogger().error(Module.HTTP, e);
			}
		}
		return content.toString();
	}
	
	private String getOperationsContent() {
		
		StringBuilder content = new StringBuilder();
		content.append("\r\nh3. Operations\r\n");
		if(launch.getOperations().size() > 0){
			for(AbstractOperation operation : launch.getOperations()){	
				content.append("\r\nh4. "+operation.getIndex()+".) "+operation.getConfig().getUIName()+"\r\n\r\n");
				Status currentStatus = operation.getStatusManager().getStatus();
				OperationHistory operationHistory = previous != null ? previous.getOperation(operation.getConfig().getId()) : null;
				Status lastStatus = operationHistory != null ? operationHistory.status : null;
				
				if(lastStatus != null && lastStatus != currentStatus){
					content.append("* Status: "+getStatusIcon(lastStatus)+" -> "+getStatusIcon(currentStatus)+"\r\n");
				}else{
					content.append("* Status: "+getStatusIcon(currentStatus)+"\r\n");
				}
				String description = operation.getRuntimeDescription();
				if(!description.isEmpty()){
					content.append("* Description: "+StringTools.border(description, DESCRIPTION_MAX)+"\r\n");
				}
			}

		}else{
			content.append("_empty_\r\n");
		}
		return content.toString();
	}

	private String getLinkPageContent() throws Exception {
		
		StringBuilder content = new StringBuilder();
		content.append(
				"["+
					launch.getConfig().getName()+" - "+
					DateTools.getTextDate(launch.getStatusManager().getStart())+
				"|"+
					getLaunchPageTitle()+
				"]"+
				" "+getStatusIcon(launch.getStatusManager().getStatus())
		);
		return content.toString();
	}
	
	public static String getStatusIcon(Status status) {
		
		if(status == Status.SUCCEED){
			return "(/)";
		}else if(status == Status.ERROR){
			return "(-)";
		}else if(status == Status.PROCESSING){
			return "(?)";
		}else if(status == Status.FAILURE){
			return "(!)";
		}else if(status == Status.CANCEL){
			return "(off)";
		}else{
			return "(?)";
		}
	}
}
