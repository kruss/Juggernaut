package core.launch.trigger;

import java.util.ArrayList;
import core.launch.ILifecycleListener;
import core.launch.LaunchAgent;
import core.launch.LifecycleObject;
import core.launch.LaunchAgent.LaunchMode;
import core.launch.data.StatusManager.Status;
import core.launch.data.property.Property;
import core.launch.repository.SVNClient;
import core.launch.repository.IRepositoryClient.HistoryInfo;
import core.launch.trigger.SearchTriggerConfig.SearchMode;
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
	
	private SearchState state;
	private ArrayList<String> revisions;	
	private String revision;
	
	public SearchTrigger(Configuration configuration, Cache cache, TaskManager taskManager, Logger logger, SearchTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (SearchTriggerConfig) super.config;
		
		client = new SVNClient(taskManager, logger);
		state = null;
		revisions = new ArrayList<String>();
		revision = null;
	}
	
	@Override
	public void checkTrigger() {
		
		try{
			loadState();
			String message = (state.buildCount+1)+". "+config.getSearchMode().toString()+"-Search";
			if(state.buildMode == BuildMode.FINISHED){
				status = new TriggerStatus(message+" FINISHED", false);
			}else{
				HistoryInfo info = client.getHistory(config.getUrl(), state.startRevision, state.endRevision, HISTORY_TIMEOUT);
				if(info == null){
					status = new TriggerStatus("No history ("+state.startRevision+" - "+state.endRevision+")", false);
					state.buildMode = BuildMode.FINISHED;
				}else{
					revisions = info.getRevisions();
					if(config.getSearchMode() == SearchMode.LINEAR && revisions.size() < 1){
						status = new TriggerStatus("Invalid interval for "+message+" ("+state.startRevision+" - "+state.endRevision+")", false);
						state.buildMode = BuildMode.FINISHED;
					}else if(config.getSearchMode() == SearchMode.BINARY && revisions.size() < 2){
						status = new TriggerStatus("Invalid interval for "+message+" ("+state.startRevision+" - "+state.endRevision+")", false);
						state.buildMode = BuildMode.FINISHED;
					}else if(state.buildMode == BuildMode.START){
						status = new TriggerStatus(message+" ("+state.startRevision+")", true);
						revision = state.startRevision;
					}else if(state.buildMode == BuildMode.MID){
						status = new TriggerStatus(message+" ("+state.midRevision+")", true);
						revision = state.midRevision;
					}else if(state.buildMode == BuildMode.END){
						status = new TriggerStatus(message+" ("+state.endRevision+")", true);
						revision = state.endRevision;
					}else{
						status = new TriggerStatus("Invalid mode: "+state.buildMode.toString(), false);
					}
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
		
		if(lifecycle == Lifecycle.FINISH){
			
			LaunchAgent launch = (LaunchAgent)object;
			Status status = launch.getStatusManager().getStatus();
			String message = 
				config.getSearchMode().toString()+
				"-Search for ["+launch.getConfig().getName()+"] has "+status.toString()+
				" ("+revision+")";
			if(status == Status.SUCCEED || status == Status.ERROR){
				try{
					if(config.getSearchMode() == SearchMode.LINEAR){
						continueLinearSearch(launch, message);
					}else if(config.getSearchMode() == SearchMode.BINARY){					
						continueBinarySearch(launch, message);
					}
					state.buildCount++;
					saveState();
				}catch(Exception e){
					logger.error(Module.COMMON, e);
				}
			}
		}
	}

	private void continueLinearSearch(LaunchAgent launch, String message) throws Exception {
		
		if(state.buildMode == BuildMode.START){
			if(revisions.size() <= 1){
				logger.log(Module.COMMON, message+" => FINISHED (intervall elapsed)");
				state.buildMode = BuildMode.FINISHED;
			}else{
				logger.log(Module.COMMON, message+" => continue at NEXT");
				state.startRevision = getNextRevision(revisions, revision);
				state.buildMode = BuildMode.START;
			}
		}
	}

	private void continueBinarySearch(LaunchAgent launch, String message) throws Exception {
		
		Long hash = new Long(launch.getOperationErrorHash());
		if(revisions.size() <= 2 && state.buildCount > 0){
			logger.log(Module.COMMON, message+" => FINISHED (intervall elapsed)");
			state.buildMode = BuildMode.FINISHED;
			
		}else if(state.buildMode == BuildMode.START){
			state.startRevision = revision;
			state.startErrorHash = hash;
			
			logger.log(Module.COMMON, message+" => continue at END");
			state.buildMode = BuildMode.END;
			
		}else if(state.buildMode == BuildMode.MID){
			state.midRevision = revision;
			state.midErrorHash = hash;
			String nextRevision = null;
			
			if(state.midErrorHash.longValue() != state.endErrorHash.longValue()){
				logger.log(Module.COMMON, message+" => continue at TOP");
				state.startRevision = state.midRevision;
				nextRevision = getMidRevision(revisions, state.midRevision, state.endRevision);

			}else{
				logger.log(Module.COMMON, message+" => continue at BOTTOM");
				state.endRevision = state.midRevision;
				nextRevision = getMidRevision(revisions, state.startRevision, state.midRevision);
			}
			
			if(!nextRevision.equals(state.midRevision)){
				state.midRevision = nextRevision;
				state.buildMode = BuildMode.MID;
			}else{
				logger.log(Module.COMMON, message+" => FINISHED (end of search)");
				state.buildMode = BuildMode.FINISHED;
			}
			
		}else if(state.buildMode == BuildMode.END){
			state.endRevision = revision;
			state.endErrorHash = hash;
			
			if(state.startErrorHash.longValue() == state.endErrorHash.longValue()){
				logger.log(Module.COMMON, message+" => FINISHED (equal bounds)");
				state.buildMode = BuildMode.FINISHED;
			}else{
				logger.log(Module.COMMON, message+" => continue at MID");
				state.midRevision = getMidRevision(revisions, state.startRevision, state.endRevision);
				state.buildMode = BuildMode.MID;
			}
		}
	}
	
	private String getNextRevision(ArrayList<String> revisions, String revision) {
		
		int maxIdx = revisions.size() - 1;
		int idx = revision.indexOf(revision);
		if(idx >= 0 && idx < maxIdx){
			return revisions.get(idx + 1);
		}else{
			return revisions.get(maxIdx);
		}
	}
	
	private String getMidRevision(ArrayList<String> revisions, String lowerBound, String upperBound) {
		
		ArrayList<String> intervall = new ArrayList<String>();
		boolean copy = lowerBound != null ? false : true;
		for(String revision : revisions){
			if(lowerBound != null && revision.equals(lowerBound)){
				copy = true;
			}
			if(copy){
				intervall.add(revision);
			}
			if(upperBound != null && revision.equals(upperBound)){
				break;
			}
		}
		String mid = intervall.get(intervall.size() / 2);
		return mid;
	}

	private enum BuildMode { START, MID, END, FINISHED }
	private enum VALUE { 
			BUILD_MODE, BUILD_COUNT, 
			REVISION_START, REVISION_MID, REVISION_END, 
			ERROR_HASH_START, ERROR_HASH_MID, ERROR_HASH_END 
	}
	
	private class SearchState {
		BuildMode buildMode;
		int buildCount;
		String startRevision;
		String midRevision;
		String endRevision;
		Long startErrorHash;
		Long midErrorHash;
		Long endErrorHash;
		
		public SearchState(SearchTriggerConfig config){
			buildMode = BuildMode.START;
			buildCount = 0;
			startRevision = config.getStartRevision();
			midRevision = null;
			endRevision = config.getEndRevision();
			startErrorHash = null;
			midErrorHash = null;
			endErrorHash = null;
		}
		
		public SearchState(
				String buildModeProperty, 
				String buildCountProperty,
				String startRevisionProperty,
				String midRevisionProperty, 
				String endRevisionProperty, 
				String startErrorHashProperty, 
				String midErrorHashProperty,
				String endErrorHashProperty)
		{
			buildMode = BuildMode.valueOf(buildModeProperty);
			buildCount = new Integer(buildCountProperty).intValue();
			startRevision = startRevisionProperty;
			midRevision = midRevisionProperty;
			endRevision = endRevisionProperty;
			startErrorHash = startErrorHashProperty != null ? new Long(startErrorHashProperty) : null;
			midErrorHash = midErrorHashProperty != null ? new Long(midErrorHashProperty) : null;
			endErrorHash = endErrorHashProperty != null ? new Long(endErrorHashProperty) : null;
		}
	}
	
	public void loadState(){
		
		String buildModeProperty = cache.getValue(config.getId(), VALUE.BUILD_MODE.toString());
		if(buildModeProperty != null){
			state = new SearchState(
				buildModeProperty,
				cache.getValue(config.getId(), VALUE.BUILD_COUNT.toString()),
				cache.getValue(config.getId(), VALUE.REVISION_START.toString()),
				cache.getValue(config.getId(), VALUE.REVISION_MID.toString()),
				cache.getValue(config.getId(), VALUE.REVISION_END.toString()),
				cache.getValue(config.getId(), VALUE.ERROR_HASH_START.toString()),
				cache.getValue(config.getId(), VALUE.ERROR_HASH_MID.toString()),
				cache.getValue(config.getId(), VALUE.ERROR_HASH_END.toString())
			);
		}else{
			state = new SearchState(config);
		}
	}
	
	public void resetState(){
		cache.removeValues(config.getId());
	}
	
	public void saveState(){
		resetState();
		cache.setValue(config.getId(), VALUE.BUILD_MODE.toString(), state.buildMode.toString());
		cache.setValue(config.getId(), VALUE.BUILD_COUNT.toString(), ""+state.buildCount);
		if(state.startRevision != null){
			cache.setValue(config.getId(), VALUE.REVISION_START.toString(), state.startRevision);
		}
		if(state.midRevision != null){
			cache.setValue(config.getId(), VALUE.REVISION_MID.toString(), state.midRevision);
		}
		if(state.endRevision != null){
			cache.setValue(config.getId(), VALUE.REVISION_END.toString(), state.endRevision);
		}
		if(state.startErrorHash != null){
			cache.setValue(config.getId(), VALUE.ERROR_HASH_START.toString(), ""+state.startErrorHash);
		}
		if(state.midErrorHash != null){
			cache.setValue(config.getId(), VALUE.ERROR_HASH_MID.toString(), ""+state.midErrorHash);
		}
		if(state.endErrorHash != null){
			cache.setValue(config.getId(), VALUE.ERROR_HASH_END.toString(), ""+state.endErrorHash);
		}
	}
}
