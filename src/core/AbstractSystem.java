package core;

import java.util.ArrayList;

public class AbstractSystem implements ISystemComponent {

	private ArrayList<ISystemComponent> components;
	
	public AbstractSystem(){
		components = new ArrayList<ISystemComponent>();
	}

	public void clear(){
		components.clear();
	}
	
	public void add(ISystemComponent component){
		components.add(component);
	}
	
	@Override
	public void init() throws Exception {
		for(int i=0; i<components.size(); i++){
			components.get(i).init();
		}
	}

	@Override
	public void shutdown() throws Exception {
		for(int i=components.size()-1; i>=0; i--){
			components.get(i).shutdown();
		}
	}
}
