package lifecycle;

public interface ILifecycleListener {

	public enum Lifecycle { START, PROCESSING, FINISH }
	
	public void lifecycleChanged(AbstractLifecycleObject object, Lifecycle lifecycle);
}
