package model;

public interface IModelListener {

	public enum Status { INIT, CHANGED, CLOSE }
	
	public void modelChanged(Status status);
}
