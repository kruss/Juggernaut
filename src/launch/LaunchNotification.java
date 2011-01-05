package launch;

import http.IHttpServer;

import java.util.ArrayList;

import core.Cache;
import core.History;
import data.AbstractOperation;
import data.Error;
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

		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		
		if(isStatusHashChanged()){
			launch.getLogger().log(Module.SMTP, "Status-Notification required");
			StatusNotification notification = new StatusNotification(history, smtpClient, httpServer, launch);
			Artifact artifact = notification.performNotification();
			artifacts.add(artifact);
			setStatusHashProperty();
		}
		
		if(isErrorHashChanged()){
			if(isErrorPresent()){
				launch.getLogger().log(Module.SMTP, "Error-Notification required");
				ErrorNotification notification = new ErrorNotification(history, smtpClient, httpServer, launch);
				Artifact artifact = notification.performNotification();
				artifacts.add(artifact);
			}
			setErrorHashProperty();
		}
		
		if(artifacts.size() > 0){
			Artifact artifact = new Artifact("Notification");
			artifact.childs = artifacts;
			launch.getArtifacts().add(artifact);
		}
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
	
	private boolean isErrorPresent() {
		return launch.getNotificationErrors().size() > 0;
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
		for(Error error : launch.getNotificationErrors()){
			hash += error.getHash();
		}
		return new Long(hash);
	}
}
