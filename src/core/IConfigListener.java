package core;

public interface IConfigListener {

	public enum Status { INIT, CHANGED, CLOSE }
	
	public void configChanged(Status status);
}
