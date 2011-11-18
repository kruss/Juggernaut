package core;

import java.util.ArrayList;

import util.UiTools;

public class AbstractSystem implements ISystemComponent {

	public static final Exception ABORT_EXCEPTION = new Exception("abort");
	
	private ArrayList<ISystemComponent> components;
	private boolean init;
	protected SystemMonitor monitor;
	
	public boolean isInitialized(){ return init; }
	public void setMonitor(SystemMonitor monitor){ this.monitor = monitor; }
	
	public AbstractSystem(){
		
		components = new ArrayList<ISystemComponent>();
		init = false;
		monitor = null;
	}

	public void clear(){
		components.clear();
	}
	
	public void add(ISystemComponent component){
		components.add(component);
		if(component instanceof AbstractSystem){
			((AbstractSystem)component).setMonitor(monitor);
		}
	}
	
	public void remove(ISystemComponent component){
		components.remove(component);
	}

	public int getComponentCount(boolean recusive) {
		
		int count = 0;
		for(ISystemComponent component : components){
			count++;
			if(recusive && component instanceof AbstractSystem){
				count += ((AbstractSystem)component).getComponentCount(recusive);
			}
		}
		return count;
	}
	
	@Override
	public void init() throws Exception {
		
		String name = getClass().getSimpleName();
		for(int i=0; i<components.size(); i++){
			ISystemComponent component = components.get(i);
			String componentName = component.getClass().getSimpleName();
			print("INIT: "+name+"::"+componentName);
			try{
				if(monitor != null){
					monitor.progress(name+"::"+componentName);
				}
				component.init();
			}catch(Exception e){
				if(e != ABORT_EXCEPTION){
					error(e);
					if(!UiTools.confirmDialog(name+" Error on INIT !!!\nContinue anyway ?", e)){
						throw ABORT_EXCEPTION;
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
		String name = getClass().getSimpleName();
		for(int i=components.size()-1; i>=0; i--){
			ISystemComponent component = components.get(i);
			String componentName = component.getClass().getSimpleName();
			print("SHUTDOWN: "+name+"::"+componentName);
			try{
				if(monitor != null){
					monitor.progress(name+"::"+componentName);
				}
				component.shutdown();
			}catch(Exception e){
				if(e != ABORT_EXCEPTION){
					error(e);
					UiTools.errorDialog(name+" Error on SHUTDOWN !!!", e);
					throw ABORT_EXCEPTION;
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
