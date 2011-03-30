package core.launch.trigger;

import java.util.ArrayList;
import java.util.Collections;

import core.launch.ILifecycleListener;
import core.launch.LaunchAgent;
import core.launch.LifecycleObject;
import core.launch.LaunchAgent.LaunchMode;
import core.launch.data.StatusManager.Status;
import core.launch.data.property.Property;
import core.launch.repository.SVNClient;
import core.launch.repository.IRepositoryClient.HistoryInfo;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class SearchTrigger extends AbstractTrigger implements ILifecycleListener {

	public static final int HISTORY_TIMEOUT = 90 * 1000; // 90 sec
	public enum PROPERTY { REVISION };
	
	private SVNClient client;
	private SearchTriggerConfig config;
	
	private HistoryInfo info;
	
	private SearchState state;
	private String revision;
	
	public SearchTrigger(Configuration configuration, Cache cache, TaskManager taskManager, Logger logger, SearchTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (SearchTriggerConfig) super.config;
		
		client = new SVNClient(taskManager, logger);
		state = null;
		revision = null;
	}
	
	@Override
	public void checkTrigger() {
		
		try{
			loadState();
			if(state.buildMode == BuildMode.FINISHED){
				status = new TriggerStatus("Search FINISHED", false);
			}else{
				info = client.getHistory(config.getUrl(), state.startRevision, state.endRevision, HISTORY_TIMEOUT);
				if(info == null){
					status = new TriggerStatus("No history ("+state.startRevision+" - "+state.endRevision+")", false);
					state.buildMode = BuildMode.FINISHED;
				}else if(info.commits.size() <= 1){
					status = new TriggerStatus("No interval ("+state.startRevision+" - "+state.endRevision+")", false);
					state.buildMode = BuildMode.FINISHED;
				}else if(state.buildMode == BuildMode.START){
					status = new TriggerStatus("Searching ("+state.startRevision+")", true);
					revision = state.startRevision;
				}else if(state.buildMode == BuildMode.END){
					status = new TriggerStatus("Searching ("+state.endRevision+")", true);
					revision = state.endRevision;
				}else{
					status = new TriggerStatus("Invalid mode: "+state.buildMode.toString(), false);
				}
				saveState();
			}

		}catch(Exception e){
			logger.error(Module.COMMON, e);
			status = new TriggerStatus(
					e.getMessage(), false
			);
		}
	}

	@Override
	public void wasTriggered(LaunchAgent launch) {
		
		if(revision != null){
			launch.setMode(LaunchMode.SEARCH);
			launch.getPropertyContainer().setProperty(new Property(config.getId(), PROPERTY.REVISION.toString(), revision));
			launch.addListener(this);
		}
	}

	@Override
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle) {
		
		if(lifecycle == Lifecycle.FINISH && info != null && state != null && revision != null){
			
			LaunchAgent launch = (LaunchAgent)object;
			String name = launch.getConfig().getName();
			Status status = launch.getStatusManager().getStatus();
			Long hash = new Long(launch.getOperationErrorHash());
			
			if(status == Status.SUCCEED || status == Status.ERROR){
				String message = "SEARCH: ["+name+"] has "+status.toString()+" at "+revision;

				if(state.buildMode == BuildMode.START){
					
					if(state.endErrorHash == null){
						logger.log(Module.COMMON, message+" => continue at END");
						state.startErrorHash = hash;
						state.buildMode = BuildMode.END;
					}else if(state.endErrorHash != hash){
						logger.log(Module.COMMON, message+" => continue at TOP");
						state.startRevision = getMidRevision(info.getRevisions(), revision, null, true);
						state.buildMode = BuildMode.START;
					}else{
						logger.log(Module.COMMON, message+" => continue at BOTTOM");
						state.endRevision = revision;
						state.startRevision = getMidRevision(info.getRevisions(), null, revision, true);
						state.buildMode = BuildMode.START;
					}
					
				}else if(state.buildMode == BuildMode.END){
					
					if(state.startErrorHash == hash){
						logger.log(Module.COMMON, message+" => equal bounds");
						state.buildMode = BuildMode.FINISHED;
					}else{
						logger.log(Module.COMMON, message+" => continue at MID");
						state.endErrorHash = hash;
						state.startRevision = getMidRevision(info.getRevisions(), null, null, true);
						state.buildMode = BuildMode.START;
					}
				}
					
				saveState();
			}
		}
	}
	
	private String getMidRevision(ArrayList<String> revisions, String lowerBound, String upperBound, boolean revert) {
		
		ArrayList<String> intervall = new ArrayList<String>();
		intervall.addAll(revisions);
		if(revert){
			Collections.reverse(intervall);
		}
		ArrayList<String> part = new ArrayList<String>();
		boolean copy = lowerBound != null ? false : true;
		for(String revision : intervall){
			if(lowerBound != null && revision.equals(lowerBound)){
				copy = true;
			}
			if(copy){
				part.add(revision);
			}
			if(upperBound != null && revision.equals(upperBound)){
				break;
			}
		}
		String mid = part.get(part.size() / 2);
		return mid;
	}

	private enum BuildMode { START, END, FINISHED }
	private enum VALUE { BUILD_MODE, REVISION_START, REVISION_END, ERROR_HASH_START, ERROR_HASH_END }
	
	private class SearchState {
		BuildMode buildMode;
		String startRevision;
		String endRevision;
		Long startErrorHash;
		Long endErrorHash;
		
		public SearchState(SearchTriggerConfig config){
			buildMode = BuildMode.START;
			startRevision = config.getStartRevision();
			endRevision = config.getEndRevision();
			startErrorHash = null;
			endErrorHash = null;
		}
		
		public SearchState(
				String buildModeProperty, 
				String startRevisionProperty, 
				String endRevisionProperty, 
				String startErrorHashProperty, 
				String endErrorHashProperty)
		{
			buildMode = BuildMode.valueOf(buildModeProperty);
			startRevision = startRevisionProperty;
			endRevision = endRevisionProperty;
			startErrorHash = startErrorHashProperty != null ? new Long(startErrorHashProperty) : null;
			endErrorHash = endErrorHashProperty != null ? new Long(endErrorHashProperty) : null;
		}
	}
	
	public void loadState(){
		
		String buildModeProperty = cache.getValue(config.getId(), VALUE.BUILD_MODE.toString());
		if(buildModeProperty != null){
			state = new SearchState(
				buildModeProperty,
				cache.getValue(config.getId(), VALUE.REVISION_START.toString()),
				cache.getValue(config.getId(), VALUE.REVISION_END.toString()),
				cache.getValue(config.getId(), VALUE.ERROR_HASH_START.toString()),
				cache.getValue(config.getId(), VALUE.ERROR_HASH_END.toString())
			);
		}else{
			state = new SearchState(config);
		}
	}
	
	public void saveState(){
		cache.setValue(config.getId(), VALUE.BUILD_MODE.toString(), state.buildMode.toString());
		if(state.startRevision != null){
			cache.setValue(config.getId(), VALUE.REVISION_START.toString(), state.startRevision);
		}else{
			cache.removeValue(config.getId(), VALUE.REVISION_START.toString());
		}
		if(state.endRevision != null){
			cache.setValue(config.getId(), VALUE.REVISION_END.toString(), state.endRevision);
		}else{
			cache.removeValue(config.getId(), VALUE.REVISION_END.toString());
		}
		if(state.startErrorHash != null){
			cache.setValue(config.getId(), VALUE.ERROR_HASH_START.toString(), ""+state.startErrorHash);
		}else{
			cache.removeValue(config.getId(), VALUE.ERROR_HASH_START.toString());
		}
		if(state.endErrorHash != null){
			cache.setValue(config.getId(), VALUE.ERROR_HASH_END.toString(), ""+state.endErrorHash);
		}else{
			cache.removeValue(config.getId(), VALUE.ERROR_HASH_END.toString());
		}
	}
}
