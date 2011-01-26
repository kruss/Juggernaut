package ui;

public interface IStatusProvider {

	public void setClient(IStatusClient client);
	public void status(String text);
}
