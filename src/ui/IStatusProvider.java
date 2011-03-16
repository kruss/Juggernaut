package ui;

public interface IStatusProvider {

	public void setStatusClient(IStatusClient client);
	public void status(String text);
}
