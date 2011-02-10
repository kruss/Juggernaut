package core.launch.notification;


import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.Error;
import core.launch.data.StatusManager.Status;
import core.launch.operation.AbstractOperation;
import core.persistence.Cache;
import core.persistence.History;
import core.runtime.http.IHttpServer;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;



/** performs the notification for a launch */
public class NotificationManager {

	private enum PROPERTY { STATUS_HASH, ERROR_HASH };
	
	private History history;
	private Cache cache;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchAgent launch;
	
	public NotificationManager(
			History history, Cache cache, ISmtpClient smtpClient, IHttpServer httpServer, LaunchAgent launch
	){
		this.history = history;
		this.cache = cache;
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.launch = launch;
	}

	public void performNotification() throws Exception {

		if(isStatusValid()){
			boolean statusChanged = isStatusHashChanged();
			if(statusChanged){
				launch.getLogger().log(Module.SMTP, "Notification required for Status");
			}
			boolean errorChanged = isErrorHashChanged();
			if(errorChanged){
				launch.getLogger().log(Module.SMTP, "Notification required for Errors");
			}
			if(statusChanged || errorChanged){
				Notification notification = new Notification(history, smtpClient, httpServer, launch);
				Artifact artifact = notification.performNotification();
				launch.getArtifacts().add(artifact);
				
				setStatusHash();
				setErrorHash();
			}else{
				launch.getLogger().debug(Module.SMTP, "Notification NOT required");
			}
		}
	}

	private boolean isStatusValid() {
		return launch.getStatusManager().getStatus() != Status.CANCEL;
	}

	private boolean isStatusHashChanged() {
		
		Long last = getStatusHash();
		Long current = computeStatusHash();
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setStatusHash(){
		
		cache.setValue(
				launch.getConfig().getId(), PROPERTY.STATUS_HASH.toString(), ""+computeStatusHash().longValue()
		);
	}
	
	private Long getStatusHash(){
		
		String value = cache.getValue(
				launch.getConfig().getId(), PROPERTY.STATUS_HASH.toString()
		);
		if(value != null){
			return new Long(value);
		}else{
			return null;
		}
	}
	
	private Long computeStatusHash() {
		
		long hash = 0;
		hash += launch.getStatusManager().getHash();
		for(AbstractOperation operation : launch.getOperations()){
			hash += operation.getStatusManager().getHash();
		}
		return new Long(hash);
	}
	
	private boolean isErrorHashChanged() {
		
		Long last = getErrorHash();
		Long current = computeErrorHash().longValue();
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setErrorHash(){
		
		cache.setValue(
				launch.getConfig().getId(), PROPERTY.ERROR_HASH.toString(), ""+computeErrorHash().longValue()
		);
	}
	
	private Long getErrorHash(){

		String value = cache.getValue(
				launch.getConfig().getId(), PROPERTY.ERROR_HASH.toString()
		);
		if(value != null){
			return new Long(value);
		}else{
			return null;
		}
	}
	
	private Long computeErrorHash() {
		
		long hash = 0;
		for(Error error : launch.getOperationErrors()){
			hash += error.getHash();
		}
		return new Long(hash);
	}
}
