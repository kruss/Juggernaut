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
	
	private SearchMeta meta;
	private ArrayList<String> revisions;	
	private String revision;
	
	public SearchTrigger(Configuration configuration, Cache cache, TaskManager taskManager, Logger logger, SearchTriggerConfig config) {
		super(configuration, cache, logger, config);
		this.config = (SearchTriggerConfig) super.config;
		
		client = new SVNClient(taskManager, logger);
		meta = null;
		revisions = new ArrayList<String>();
		revision = null;
	}
	
	@Override
	public void checkTrigger() {
		
		try{
			loadSearchMeta();
			if(meta.state == SearchState.FINISHED){
				status = new TriggerStatus("Search FINISHED", false);
			}else{
				String startRevision = meta.revision;
				String endRevision = config.getEndRevision();
				HistoryInfo history = client.getHistory(config.getUrl(), startRevision, endRevision, HISTORY_TIMEOUT);
				if(history == null){
					status = new TriggerStatus("No history ("+startRevision+" - "+endRevision+")", false);
					meta.state = SearchState.FINISHED;
				}else{
					revisions = history.getRevisions();
					revision = startRevision;
					status = new TriggerStatus((meta.count+1)+". Search ("+startRevision+")", true);
				}
				saveSearchMeta();
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
			String info = (meta.count+1)+". Search ["+launch.getConfig().getName()+"] has "+status.toString()+" ("+revision+")";
			if(status == Status.SUCCEED || status == Status.ERROR){
				if(revisions.size() < config.getRevisionIntervall()){
					logger.log(Module.COMMON, info+" => FINISHED");
					meta.state = SearchState.FINISHED;
				}else{
					logger.log(Module.COMMON, info+" => CONTINUE");
					meta.revision = getNextRevision(revisions, revision, config.getRevisionIntervall());
				}
				meta.count++;
				saveSearchMeta();
			}
		}
	}
	
	private String getNextRevision(ArrayList<String> revisions, String revision, int intervall) {
		
		int maxIdx = revisions.size() - 1;
		int currentIdx = revisions.indexOf(revision);
		if(currentIdx >= 0 && (currentIdx + intervall) < maxIdx){
			return revisions.get(currentIdx + intervall);
		}else{
			return revisions.get(maxIdx);
		}
	}

	private enum SearchState { 
		SEARCH, FINISHED 
	}
	
	private enum VALUE { 
		STATE, COUNT, REVISION
	}
	
	private class SearchMeta {
		SearchState state;
		int count;
		String revision;
		
		public SearchMeta(SearchTriggerConfig config){
			state = SearchState.SEARCH;
			count = 0;
			revision = config.getStartRevision();
		}
		
		public SearchMeta(
				String stateProperty, 
				String countProperty,
				String revisionProperty)
		{
			state = SearchState.valueOf(stateProperty);
			count = new Integer(countProperty).intValue();
			revision = revisionProperty;
		}
	}
	
	public void loadSearchMeta(){
		String stateProperty = cache.getValue(config.getId(), VALUE.STATE.toString());
		if(stateProperty != null){
			meta = new SearchMeta(
				stateProperty,
				cache.getValue(config.getId(), VALUE.COUNT.toString()),
				cache.getValue(config.getId(), VALUE.REVISION.toString())
			);
		}else{
			meta = new SearchMeta(config);
		}
	}
	
	public void saveSearchMeta(){
		resetSearchMeta();
		cache.setValue(config.getId(), VALUE.STATE.toString(), meta.state.toString());
		cache.setValue(config.getId(), VALUE.COUNT.toString(), ""+meta.count);
		cache.setValue(config.getId(), VALUE.REVISION.toString(), meta.revision);
	}
	
	public void resetSearchMeta(){
		cache.removeValues(config.getId());
	}
}
