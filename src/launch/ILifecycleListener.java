package launch;

public interface ILifecycleListener {

	public enum Lifecycle { START, PROCESSING, FINISH }
	
	public void lifecycleChanged(LifecycleObject object, Lifecycle lifecycle);
}
