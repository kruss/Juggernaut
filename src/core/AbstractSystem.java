package core;

import java.util.ArrayList;

import util.UiTools;

public class AbstractSystem implements ISystemComponent {

	public static final Exception ABOARDING = new Exception("aboarding");
	
	private ArrayList<ISystemComponent> components;
	private boolean init;
	
	public boolean isInitialized(){ return init; }
	
	public AbstractSystem(){
		
		components = new ArrayList<ISystemComponent>();
		init = false;
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
			ISystemComponent component = components.get(i);
			String name = (getClass().getSimpleName())+"::"+component.getClass().getSimpleName();
			print("INIT: "+name);
			try{
				component.init();
			}catch(Exception e){
				if(e != ABOARDING){
					error(e);
					if(!UiTools.confirmDialog(name+" Error on INIT!\nContinue anyway ?", e)){
						throw ABOARDING;
					}
				}else{
					throw e;
				}
			}
		}
		init = true;
	}

	@Override
	public void shutdown() throws Exception {
		
		init = false;
		for(int i=components.size()-1; i>=0; i--){
			ISystemComponent component = components.get(i);
			String name = (getClass().getSimpleName())+"::"+component.getClass().getSimpleName();
			print("SHUTDOWN: "+name);
			try{
				component.shutdown();
			}catch(Exception e){
				if(e != ABOARDING){
					error(e);
					UiTools.errorDialog(name+" Error on SHUTDOWN!", e);
					throw ABOARDING;
				}else{
					throw e;
				}
			}
		}
	}
	
	private void print(String text){
		System.out.println(text);
	}
	
	private void error(Exception e) {
		e.printStackTrace();
	}
}
