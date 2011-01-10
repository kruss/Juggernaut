package launch;

import http.IHttpServer;

import core.Cache;
import core.History;
import data.AbstractOperation;
import data.Error;
import launch.StatusManager.Status;
import logger.ILogConfig.Module;

import data.Artifact;

import smtp.ISmtpClient;

/** performs the notification for a launch */
public class LaunchNotification {

	private enum Property { STATUS_HASH, ERROR_HASH };
	
	private History history;
	private Cache cache;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchAgent launch;
	
	public LaunchNotification(
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
				
				setStatusHashProperty();
				setErrorHashProperty();
			}else{
				launch.getLogger().debug(Module.SMTP, "Notification NOT required");
			}
		}
	}

	private boolean isStatusValid() {
		return launch.getStatusManager().getStatus() != Status.CANCEL;
	}

	private boolean isStatusHashChanged() {
		
		Long last = getStatusHashProperty();
		Long current = computeStatusHash();
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setStatusHashProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.STATUS_HASH.toString(), ""+computeStatusHash().longValue()
		);
	}
	
	private Long getStatusHashProperty(){
		
		String value = cache.getProperty(
				launch.getConfig().getId(), Property.STATUS_HASH.toString()
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
		
		Long last = getErrorHashProperty();
		Long current = computeErrorHash().longValue();
		return (last == null) || (last.longValue() != current.longValue());
	}
	
	private void setErrorHashProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.ERROR_HASH.toString(), ""+computeErrorHash().longValue()
		);
	}
	
	private Long getErrorHashProperty(){

		String value = cache.getProperty(
				launch.getConfig().getId(), Property.ERROR_HASH.toString()
		);
		if(value != null){
			return new Long(value);
		}else{
			return null;
		}
	}
	
	private Long computeErrorHash() {
		
		long hash = 0;
		for(Error error : launch.getErrors()){
			hash += error.getHash();
		}
		return new Long(hash);
	}
}
