package launch;

import http.IHttpServer;

import java.util.ArrayList;

import core.Cache;

import launch.StatusManager.Status;
import data.Error;
import logger.ILogConfig.Module;

import data.Artifact;

import smtp.ISmtpClient;

/** performs the notification for a launch */
public class LaunchNotification {

	private enum Property { STATUS, ERRORS };
	
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
		
		if(isStatusChanged()){
			launch.getLogger().log(Module.SMTP, "Status-Notification required");
			StatusNotification notification = new StatusNotification(client, server, launch);
			Artifact artifact = notification.performNotification();
			artifacts.add(artifact);
			setLastStatusProperty();
		}
		
		if(isErrorChanged()){
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

	private boolean isStatusChanged() {
		
		Status last = getLastStatusProperty();
		Status current = launch.getStatusManager().getStatus();
		return (last == null) || (last != current);
	}
	
	private void setLastStatusProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.STATUS.toString(), launch.getStatusManager().getStatus().toString()
		);
	}
	
	private Status getLastStatusProperty(){
		
		String value = cache.getProperty(
				launch.getConfig().getId(), Property.STATUS.toString()
		);
		if(value != null){
			return Status.valueOf(value);
		}else{
			return null;
		}
	}
	
	private boolean isErrorPresent() {
		return launch.getNotificationErrors().size() > 0;
	}
	
	private boolean isErrorChanged() {
		
		Long last = getErrorHashProperty();
		long current = computeErrorHash();
		return (last == null) || (last.longValue() != current);
	}
	
	private void setErrorHashProperty(){
		
		cache.addProperty(
				launch.getConfig().getId(), Property.ERRORS.toString(), ""+computeErrorHash()
		);
	}
	
	private Long getErrorHashProperty(){

		String value = cache.getProperty(
				launch.getConfig().getId(), Property.ERRORS.toString()
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
