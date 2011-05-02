package core.launch;

import java.util.Date;

import core.launch.operation.AbstractOperation;
import core.runtime.logger.ILogListener;
import core.runtime.logger.ILogProvider;

public class ProofOfLiveMonitor implements ILogListener, ILifecycleListener {

	private ILogProvider provider;
	private Date updated;
	
	public ProofOfLiveMonitor(LaunchAgent launch){
		launch.getLogger().addListener(this);
		launch.addListener(this);
		for(AbstractOperation operation : launch.getOperations()){
			operation.addListener(this);
		}
		update();
	}
	
	public synchronized Date getUpdated(){ 
		return updated; 
	}
	
	private synchronized void update() {
		updated = new Date();
	}

	@Override
	public void logged(String log) {
		update();
	}
	
	@Override
	public void setProvider(ILogProvider provider) {
		this.provider = provider;
	}

	@Override
	public ILogProvider getProvider() {
		return provider;
	}
	
	@Override
	public void deregister() {
		if(provider != null){
			provider.removeListener(this);
		}
	}

	@Override
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle) {
		update();
	}
}
