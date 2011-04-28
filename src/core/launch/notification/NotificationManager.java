package core.launch.notification;


import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.StatusManager.Status;
import core.persistence.Cache;
import core.persistence.History;
import core.runtime.http.IHttpConfig;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;



/** performs the notification for a launch */
public class NotificationManager {

	private enum PROPERTY { STATUS_HASH, ERROR_HASH };
	
	private History history;
	private Cache cache;
	private ISmtpClient smtpClient;
	private IHttpConfig httpConfig;
	private LaunchAgent launch;
	
	public NotificationManager(
			History history, Cache cache, ISmtpClient smtpClient, IHttpConfig httpConfig, LaunchAgent launch
	){
		this.history = history;
		this.cache = cache;
		this.smtpClient = smtpClient;
		this.httpConfig = httpConfig;
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
				Notification notification = new Notification(history, smtpClient, httpConfig, launch);
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
		Long current = new Long(launch.getLaunchStatusHash());
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setStatusHash(){
		
		cache.setValue(
				launch.getConfig().getId(), PROPERTY.STATUS_HASH.toString(), ""+launch.getLaunchStatusHash()
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
	
	private boolean isErrorHashChanged() {
		
		Long last = getErrorHash();
		Long current =  new Long(launch.getOperationErrorHash());
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setErrorHash(){
		
		cache.setValue(
				launch.getConfig().getId(), PROPERTY.ERROR_HASH.toString(), ""+launch.getOperationErrorHash()
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
}
