package core;

public interface IConfigStoreListener {

	public enum State { CLEAN, DIRTY }
	
	public void configChanged(State state);
}
