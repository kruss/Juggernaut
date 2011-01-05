package launch;

import http.IHttpServer;

import java.util.ArrayList;

import core.Cache;
import data.AbstractOperation;
import data.Error;
import logger.ILogConfig.Module;

import data.Artifact;

import smtp.ISmtpClient;

/** performs the notification for a launch */
public class LaunchNotification {

	private enum Property { STATUS_HASH, ERROR_HASH };
	
	private Cache cache;
	private ISmtpClient client;
	protected IHttpServer server;
	private LaunchAgent launch;
	
	public LaunchNotification(
			Cache cache, ISmtpClient client, IHttpServer server, LaunchAgent launch
	){
		this.cache = cache;
		this.client = client;
		this.server = server;
		this.launch = launch;
	}

	public void performNotification() throws Exception {

		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		
		if(isStatusHashChanged()){
			launch.getLogger().log(Module.SMTP, "Status-Notification required");
			StatusNotification notification = new StatusNotification(client, server, launch);
			Artifact artifact = notification.performNotification();
			artifacts.add(artifact);
			setStatusHashProperty();
		}
		
		if(isErrorHashChanged()){
			if(isErrorPresent()){
				launch.getLogger().log(Module.SMTP, "Error-Notification required");
				ErrorNotification notification = new ErrorNotification(client, server, launch);
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
		long current = computeStatusHash();
		return (last == null) || (last.longValue() != current);
	}
	
	private void setStatusHashProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.STATUS_HASH.toString(), ""+computeStatusHash()
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
	
	private long computeStatusHash() {
		
		long hash = 0;
		hash += launch.getStatusManager().getHash();
		for(AbstractOperation operation : launch.getOperations()){
			hash += operation.getStatusManager().getHash();
		}
		return hash;
	}
	
	private boolean isErrorHashChanged() {
		
		Long last = getErrorHashProperty();
		long current = computeErrorHash();
		return (last == null) || (last.longValue() != current);
	}
	
	private boolean isErrorPresent() {
		return launch.getNotificationErrors().size() > 0;
	}
	
	private void setErrorHashProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.ERROR_HASH.toString(), ""+computeErrorHash()
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
	
	private long computeErrorHash() {
		
		long hash = 0;
		for(Error error : launch.getNotificationErrors()){
			hash += error.getHash();
		}
		return hash;
	}
}
