package util;

public interface IChangeable {

	public void addListener(IChangeListener listener);
	public void removeListener(IChangeListener listener);
	public void notifyListeners();
}
