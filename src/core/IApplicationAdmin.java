package core;

public interface IApplicationAdmin {

	/** drop any unsaved changes */
	public void revert() throws Exception;

	/** quit the application */
	void quit() throws Exception;

}
